package ru.max.springboot.dto.boosty;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BoostySubscriberInfo {
    private String email;
    private String status;
    private LocalDateTime nextPayTime;
}
