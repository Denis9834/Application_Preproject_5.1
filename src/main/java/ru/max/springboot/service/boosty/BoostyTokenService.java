package ru.max.springboot.service.boosty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.max.springboot.dto.boosty.BoostySession;
import ru.max.springboot.util.BoostySessionStorage;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Сервис для работы с токенами авторизации Boosty
 * <p>
 * - Загружается сохраненная сессия из файла boosty_session.json
 * - проверяется срок действия accessToken
 * - Обновляется токен с использованием refreshToken
 * - Сохраняется все обратно в файл boosty_session.json
 */

@Service
public class BoostyTokenService {
    private static final String TOKEN_URL = "https://api.boosty.to/oauth/token/";
    private static final ObjectMapper mapper = new ObjectMapper();

    public BoostySession getValidSession() {
        try {
            BoostySession session = BoostySessionStorage.load(); // загружается сессия из файла
            if (session.isExpired()) {
                return refreshToken(session);
            }
            return session;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить Boosty-сессию", e);
        }
    }

    private BoostySession refreshToken(BoostySession session) {
        try {
            //Тело запроса
            String body = buildFormData(Map.of(
                    "grant_type", "refresh_token",
                    "refresh_token", session.getRefreshToken(),
                    "device_id", session.getDeviceId(),
                    "device_os", "web"
            ));

            //запрос на обновление токена
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("User-Agent", "Mozilla/5.0")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode node = mapper.readTree(response.body());

                //Новые токены и срок действия
                String newAccessToken = node.get("access_token").asText();
                String newRefreshToken = node.get("refresh_token").asText();
                int expiresIn = node.get("expires_in").asInt();

                //Обновление объекта сессии
                session.setAccessToken(newAccessToken);
                session.setRefreshToken(newRefreshToken);
                session.setExpiresAt(System.currentTimeMillis() + expiresIn * 1000L);

                //save в файл
                BoostySessionStorage.save(session);
                return session;
            } else {
                throw new RuntimeException("Ошибка при обновлении токена Boosty: " +
                        response.statusCode() + " — " + response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обновления токена Boosty", e);
        }
    }

    /**
     * Формирование тела запроса в формате key1=value1&key2=value2..., который требуется для OAuth-запросов Boosty
     */
    private String buildFormData(Map<String, String> data) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (result.length() > 0) result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }
}
