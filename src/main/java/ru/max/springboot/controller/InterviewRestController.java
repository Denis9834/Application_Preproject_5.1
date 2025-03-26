package ru.max.springboot.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.User;
import ru.max.springboot.service.InterviewService;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewRestController {

    private final InterviewService interviewService;

    @Autowired
    public InterviewRestController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    //Получение списка всех собеседований
    @GetMapping
    public ResponseEntity<List<Interview>> getInterview(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(interviewService.getInterviewByUser(user));
    }

    // Добавление записи нового собеседования
    @PostMapping
    public ResponseEntity<Interview> createInterview(@AuthenticationPrincipal User user, @RequestBody @Valid InterviewDTO dto) {
        return ResponseEntity.ok(interviewService.createInterview(dto, user));
    }

}
