package ru.max.springboot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.dto.InterviewResponseDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.InterviewStatus;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.InterviewRepository;
import ru.max.springboot.repository.UserRepository;
import ru.max.springboot.service.InterviewService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;

    @Autowired
    public InterviewServiceImpl(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    //Получение собеседований для пользователя
    @Override
    public List<InterviewResponseDTO> getInterviewByUser(User user) {
        return interviewRepository.findByUser(user)
                .stream()
                .map((this::mapToResponseDTO))
                .collect(Collectors.toList());
    }

    //Получение всех собеседований у Admin
    public List<InterviewResponseDTO> getAllInterviews() {
        return interviewRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Создание нового собеседования
    @Override
    public Interview createInterview(InterviewDTO dto, User user) {
        Interview interview = new Interview();

        interview.setOrganization(dto.getOrganization());
        interview.setGrade(dto.getGrade());
        interview.setJobLink(dto.getJobLink());
        interview.setContact(dto.getContact());
        interview.setProject(dto.getProject());
        interview.setComments(dto.getComments());
        interview.setDataTime(dto.getDataTime());
        interview.setSalaryOffer(dto.getSalaryOffer());
        interview.setUser(user);

        return interviewRepository.save(interview);
    }

    //Редактирование собеседования
    @Override
    public Interview updateInterview(Long id, InterviewDTO dto, User user) {
        Interview interview = getByIdAndUser(id, user);

        interview.setOrganization(dto.getOrganization());
        interview.setGrade(dto.getGrade());
        interview.setJobLink(dto.getJobLink());
        interview.setContact(dto.getContact());
        interview.setProject(dto.getProject());
        interview.setDataTime(dto.getDataTime());
        interview.setSalaryOffer(dto.getSalaryOffer());
        interview.setComments(dto.getComments());
        interview.setFinalOffer(dto.getFinalOffer());
        interview.setInterviewNotes(dto.getInterviewNotes());
        interview.setStatus(dto.getStatus());
        return interviewRepository.save(interview);
    }

    // получение собеседования по id
    @Override
    public Interview getByIdAndUser(Long id, User user) {
        return interviewRepository.findById(id).orElse(null);
    }

    //Удаление собеседования
    @Override
    public void deleteInterview(Long id, User user) {
        Interview interview = getByIdAndUser(id, user);
        interviewRepository.delete(interview);
    }

    //обновление статуса на "Пройдено"
    @Override
    public void updateStatusToPassed(Long id, String notes) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Собеседование не найдено"));

        interview.setStatus(InterviewStatus.PASSED);
        interview.setInterviewNotes(notes);
        interviewRepository.save(interview);
    }

    //обновление статуса на "Оффер"
    @Override
    public void updateStatusToOffered(Long id, BigDecimal offer) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Собеседование не найдено"));

        interview.setStatus(InterviewStatus.OFFERED);
        interview.setFinalOffer(offer);
        interviewRepository.save(interview);

    }

    //Преобразование в InterviewResponseDTO
    private InterviewResponseDTO mapToResponseDTO(Interview interview) {
        InterviewResponseDTO dto = new InterviewResponseDTO();
        dto.setId(interview.getInterviewId());
        dto.setOrganization(interview.getOrganization());
        dto.setGrade(interview.getGrade());
        dto.setJobLink(interview.getJobLink());
        dto.setContact(interview.getContact());
        dto.setProject(interview.getProject());
        dto.setDataTime(interview.getDataTime());
        dto.setComments(interview.getComments());
        dto.setSalaryOffer(interview.getSalaryOffer());
        dto.setStatus(interview.getStatus());
        dto.setInterviewNotes(interview.getInterviewNotes());
        dto.setFinalOffer(interview.getFinalOffer());
        dto.setUserId(interview.getUser().getId());
        dto.setUserName(interview.getUser().getName());
        return dto;
    }
}
