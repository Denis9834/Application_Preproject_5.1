package ru.max.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    private Long id;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Column(name = "organization", nullable = false)
    private String organization;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Column(name = "grade", nullable = false)
    private String grade;

    @NotEmpty(message = "Поле не должно быть пустым")
    @Column(name = "job_link", nullable = false)
    private String jobLink;

    @Column(name = "contact")
    private String contact;

    @Column(name = "project")
    private String project;

    @Column(name = "recruiter")
    private String recruiter;

    @NotNull(message = "Поле не должно быть пустым")
    @Column(name = "interview_data_time", nullable = false)
    private LocalDateTime dataTime;

    @Column(name = "comments")
    private String comments;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
