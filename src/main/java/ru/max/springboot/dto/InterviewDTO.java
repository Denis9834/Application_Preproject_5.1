package ru.max.springboot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.max.springboot.model.InterviewStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewDTO {

    @NotEmpty(message = "Поле не должно быть пустым")
    private String organization;

    @NotEmpty(message = "Поле не должно быть пустым")
    private String grade;

    @NotEmpty(message = "Поле не должно быть пустым")
    private String jobLink;

    private String contact;

    private String project;

    private BigDecimal salaryOffer;

    private BigDecimal finalOffer;

    private String interviewNotes;

    private InterviewStatus status;

    @NotNull(message = "Поле не должно быть пустым")
    private LocalDateTime dataTime;

    private String comments;
}
