package ru.max.springboot.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum InterviewStatus {

    SCHEDULED("Запланировано"),
    PASSED("Пройдено"),
    OFFERED("Оффер");

    private final String label;
}
