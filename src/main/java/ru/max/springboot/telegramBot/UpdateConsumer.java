package ru.max.springboot.telegramBot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.max.springboot.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserService userService;
    private final Map<Long, Boolean> awaitingEmail = new ConcurrentHashMap<>();

    public UpdateConsumer(@Value("${telegram.bot.token}") String tgToken, UserService userService) {
        this.telegramClient = new OkHttpTelegramClient(tgToken);
        this.userService = userService;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (messageText != null && messageText.equals("/start")) {
                sendMainMenu(chatId);
            } else {
                if (awaitingEmail.getOrDefault(chatId, false)) {
                    awaitingEmail.remove(chatId);
                    if (isValidEmail(messageText)) {
                        String telegramUsername = update.getMessage().getFrom().getUserName();
                        userService.updateTelegramUserInfo(chatId, messageText, telegramUsername);
                        sendMessage(chatId, "Email успешно сохранён!");
                    } else {
                        sendMessage(chatId, "Неверный формат email. Попробуйте снова через /start.");
                    }
                } else {
                    sendMessage(chatId, "Введите команду /start");
                }
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();

        switch (data) {
            case "email" -> sendEmail(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");
        }
    }

    private void sendEmail(Long chatId) {
        awaitingEmail.put(chatId, true);
        sendMessage(chatId, "Пожалуйста, введите ваш Boosty email:");
    }

    private void sendMessage(Long chatId, String messageText) {
        try {
            SendMessage message = SendMessage.builder()
                    .text(messageText)
                    .chatId(chatId)
                    .build();

            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения в Telegram", e);
        }
    }

    private void sendMainMenu(Long chatId) {
        try {
            SendMessage message = SendMessage.builder()
                    .text("Добро пожаловать! Выберите действие:")
                    .chatId(chatId)
                    .build();

            var buttonEmail = InlineKeyboardButton.builder()
                    .text("Ввести Email")
                    .callbackData("email")
                    .build();

            List<InlineKeyboardRow> keyboardRows = List.of(
                    new InlineKeyboardRow(buttonEmail)
            );

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
            message.setReplyMarkup(markup);

            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке главного меню", e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
