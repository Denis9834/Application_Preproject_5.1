package ru.max.springboot.dto.boosty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoostySession {
    private String accessToken;
    private String refreshToken;
    private String deviceId;
    private Long expiresAt;

    //истек ли токен
    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }
}
