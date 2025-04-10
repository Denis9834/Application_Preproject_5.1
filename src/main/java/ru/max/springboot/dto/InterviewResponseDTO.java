package ru.max.springboot.dto;

import lombok.*;
import ru.max.springboot.model.InterviewStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InterviewResponseDTO {
    private Long id;
    private String organization;
    private String grade;
    private String jobLink;
    private String contact;
    private String project;
    private LocalDateTime dataTime;
    private String comments;
    private BigDecimal salaryOffer;
    private InterviewStatus status;
    private String interviewNotes;
    private BigDecimal finalOffer;
    private Long userId;
    private String userName;

    public String getStatusLabel() {
        return status != null ? status.getLabel() : null;
    }
}
