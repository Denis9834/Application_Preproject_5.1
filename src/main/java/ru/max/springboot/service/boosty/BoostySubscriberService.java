package ru.max.springboot.service.boosty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.max.springboot.dto.boosty.BoostySubscriberInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Сервис для получения инфы о подписчиках с Boosty.
 * Используется для валидации доступа пользователя, авторизованного через TG
 * <p>
 * - Отправляется запрос к Boosty
 * - Проверяется, есть ли активная подписка по email
 * - Возвращается инфа о подписке (статус, дата след. платежа, email)
 */
@Service
public class BoostySubscriberService {

    private final BoostyTokenService boostyTokenService;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${boosty.blog.username}")
    private String blogUsername;

    public BoostySubscriberService(BoostyTokenService boostyTokenService) {
        this.boostyTokenService = boostyTokenService;
    }

    //Запрос к Boosty и возвращает подписчика, если он есть
    public BoostySubscriberInfo fetchSubscriberInfo(String email) {
        try {
            String token = boostyTokenService.getValidSession().getAccessToken();
            String url = "https://api.boosty.to/v1/blog/" + blogUsername + "/subscribers";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            //json подписчиков
            JsonNode data = mapper.readTree(response.body()).path("data");

            for (JsonNode node : data) {
                String subEmail = node.path("email").asText();
                String status = node.path("status").asText();
                boolean subscribed = node.path("subscribed").asBoolean();
                long nextPayMs = node.path("nextPayTime").asLong();

                if (email.equalsIgnoreCase(subEmail) &&
                        "active".equalsIgnoreCase(status) &&
                        subscribed) {

                    LocalDateTime nextPayTime = Instant.ofEpochSecond(nextPayMs)
                            .atZone(ZoneId.systemDefault()).toLocalDateTime();

                    //dto информация для возврата
                    BoostySubscriberInfo info = new BoostySubscriberInfo();
                    info.setEmail(subEmail);
                    info.setStatus(status);
                    info.setNextPayTime(nextPayTime);
                    return info;
                }
            }
            return null;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Ошибка запроса к Boosty", e);
        }
    }
}
