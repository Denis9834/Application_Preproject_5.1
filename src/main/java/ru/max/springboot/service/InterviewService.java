package ru.max.springboot.service;

import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.dto.InterviewResponseDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface InterviewService {
    List<InterviewResponseDTO> getInterviewByUser(User user);

    Interview createInterview(InterviewDTO dto, User user);

    Interview updateInterview(Long id, InterviewDTO dto, User user);

    Interview getByIdAndUser(Long id, User user);

    void deleteInterview(Long id, User user);

    void updateStatusToPassed(Long id, String notes);

    void updateStatusToOffered(Long id, BigDecimal offer);

    List<InterviewResponseDTO> searchFuzzyAllByOrganization(String term);

    List<InterviewResponseDTO> searchFuzzyByUserAllByOrganization(String term, User user);
}