package ru.max.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.InterviewRepository;
import ru.max.springboot.repository.UserRepository;

import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    @Autowired
    public InterviewService(InterviewRepository interviewRepository, UserRepository userRepository) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
    }

    //Получение собеседований для пользователя
    public List<Interview> getInterviewByUser(User user) {
        return interviewRepository.findByUser(user);
    }

    //Создание нового собеседования
    public Interview createInterview(InterviewDTO dto, User user) {
        Interview interview = new Interview();
        interview.setOrganization(dto.getOrganization());
        interview.setGrade(dto.getGrade());
        interview.setJobLink(dto.getJobLink());
        interview.setContact(dto.getContact());
        interview.setProject(dto.getProject());
        interview.setRecruiter(dto.getRecruiter());
        interview.setComments(dto.getComments());
        interview.setDataTime(dto.getDataTime());
        interview.setUser(user);
        return interviewRepository.save(interview);
    }

}
