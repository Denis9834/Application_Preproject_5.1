package ru.max.springboot.telegramBot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class TelegramBot implements SpringLongPollingBot {

    private final UpdateConsumer consumer;

    public TelegramBot(UpdateConsumer consumer) {
        this.consumer = consumer;
    }

    @Value("${telegram.bot.token}")
    private String tgToken;


    @Override
    public String getBotToken() {
        return tgToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return consumer;
    }
}
