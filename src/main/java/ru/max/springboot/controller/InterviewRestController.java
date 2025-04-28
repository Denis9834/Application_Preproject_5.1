package ru.max.springboot.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.dto.InterviewResponseDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.User;
import ru.max.springboot.service.impl.InterviewServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
public class InterviewRestController {

    private final InterviewServiceImpl interviewServiceImpl;

    @Autowired
    public InterviewRestController(InterviewServiceImpl interviewServiceImpl) {
        this.interviewServiceImpl = interviewServiceImpl;
    }

    //Получение списка всех собеседований
    @GetMapping
    public ResponseEntity<List<InterviewResponseDTO>> getInterview(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(interviewServiceImpl.getInterviewByUser(user));
    }

    // Добавление записи нового собеседования
    @PostMapping
    public ResponseEntity<Interview> createInterview(@AuthenticationPrincipal User user, @RequestBody @Valid InterviewDTO dto) {
        return ResponseEntity.ok(interviewServiceImpl.createInterview(dto, user));
    }

    //Удаление собеседования по id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInterview(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        interviewServiceImpl.deleteInterview(id, currentUser);
        return ResponseEntity.ok("Запись успешно удалена!");
    }

    //получение собеседования по id
    @GetMapping("/{id}")
    public ResponseEntity<Interview> getInterviewById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(interviewServiceImpl.getByIdAndUser(id, user));
    }

    //Редактирование собеседования
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInterview(@PathVariable Long id,
                                             @AuthenticationPrincipal User user,
                                             @RequestBody @Valid InterviewDTO interviewDTO) {
        try {
            Interview update = interviewServiceImpl.updateInterview(id, interviewDTO, user);
            return ResponseEntity.ok(update);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Прошел собеседование, запись "Что спрашивали"
    @PutMapping("/{id}/passed")
    public ResponseEntity<?> updateStatusToPassed(@PathVariable Long id,
                                                  @RequestBody Map<String, String> payload) {
        String notes = payload.get("notes");
        interviewServiceImpl.updateStatusToPassed(id, notes);
        return ResponseEntity.ok("Собеседование пройдено!");
    }

    // статус "Получен оффер", сумма оффера
    @PutMapping("/{id}/offer")
    public ResponseEntity<?> updateStatusToOffer(@PathVariable Long id,
                                                 @RequestBody Map<String, Object> payload) {
        BigDecimal offer = new BigDecimal(payload.get("offer").toString());
        interviewServiceImpl.updateStatusToOffered(id, offer);
        return ResponseEntity.ok("Оффер добавлен");
    }

    // Все собеседования для Admin
    @GetMapping("/all")
    public ResponseEntity<List<InterviewResponseDTO>> getAllInterviews() {
        return ResponseEntity.ok(interviewServiceImpl.getAllInterviews());
    }

    //search fuzzy Admin
    @GetMapping("/all/search")
    public ResponseEntity<List<InterviewResponseDTO>> searchAdmin(@RequestParam String term) {
        return ResponseEntity.ok(interviewServiceImpl.searchFuzzyAllByOrganization(term));
    }

    //search fuzzy User
    @GetMapping("/search")
    public ResponseEntity<List<InterviewResponseDTO>> searchUser(@RequestParam String term,
                                                                 @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(interviewServiceImpl.searchFuzzyByUserAllByOrganization(term, user));
    }
}
