package ru.max.springboot.service.impl;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.dto.InterviewResponseDTO;
import ru.max.springboot.model.Interview;
import ru.max.springboot.model.InterviewStatus;
import ru.max.springboot.model.Role;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.InterviewRepository;
import ru.max.springboot.repository.UserRepository;
import ru.max.springboot.service.InterviewService;
import ru.max.springboot.service.RoleService;
import ru.max.springboot.service.UserService;
import ru.max.springboot.util.Transliteration;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public InterviewServiceImpl(InterviewRepository interviewRepository, UserService userService, RoleService roleService) {
        this.interviewRepository = interviewRepository;
        this.userService = userService;
        this.roleService = roleService;
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
        interview.setOrganizationLatin(Transliteration.toLatin(dto.getOrganization()));
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
        interview.setOrganizationLatin(Transliteration.toLatin(dto.getOrganization()));
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

    //Неточный поиск у Admin
    @Override
    public List<InterviewResponseDTO> searchFuzzyAllByOrganization(String term) {
        String ternLatin = Transliteration.toLatin(term);
        return interviewRepository.searchFuzzyAllByOrganization(term, ternLatin)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //Неточный поиск у User
    @Override
    public List<InterviewResponseDTO> searchFuzzyByUserAllByOrganization(String term, User user) {
        String termLatin = Transliteration.toLatin(term);
        return interviewRepository.searchFuzzyByUserAllByOrganization(user.getId(), term, termLatin)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void importFromExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter();
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // пропускаем первую строку (заголовок)

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) { //проход по всем строкам со второй до последней не пустой
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                String email = String.valueOf(row.getCell(1));
                if (email.isEmpty()) {
                    continue;
                }

                String importedStatus = String.valueOf(row.getCell(8)).trim();
                InterviewStatus status = (importedStatus.equalsIgnoreCase("offer")
                        || importedStatus.equalsIgnoreCase("apply"))
                        ? InterviewStatus.OFFERED
                        : InterviewStatus.PASSED;

                Optional<User> optUser = Optional.ofNullable(userService.findByEmail(email));
                User user = optUser.orElseGet(() -> {

                    User u = new User();
                    u.setName("-");
                    u.setEmail(email);
                    u.setPassword("import");
                    Role userRole = roleService.findByRole("ROLE_USER")
                            .orElseThrow(() -> new RuntimeException("Ошибка при назначении роли пользователю"));

                    u.setRoles(Set.of(userRole));
                    u.setAge(18);
                    return userService.save(u);
                });

                LocalDateTime importedDateTime = row.getCell(0).getLocalDateTimeCellValue();
                String importedOrganization = String.valueOf(row.getCell(2));
                String importedProject = String.valueOf(row.getCell(3));
                String importedInterviewNote = String.valueOf(row.getCell(5));

                // парсинг 6 столба как строку, даже если в Excel она числовая
                String salaryText = formatter.formatCellValue(row.getCell(6)).trim();
                BigDecimal importedSalaryOffer = null;
                if (!salaryText.isEmpty()) {
                    try {
                        importedSalaryOffer = new BigDecimal(salaryText);
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Неверный формат: " + salaryText, ex);
                    }
                }
                BigDecimal importedOffer = (status == InterviewStatus.OFFERED)
                        ? importedSalaryOffer
                        : null;

                String importedGrade = String.valueOf(row.getCell(7));
                final String JOB_LINK = "https://hh.ru";

                Interview interview = new Interview();
                interview.setDataTime(importedDateTime);
                interview.setOrganization(importedOrganization);
                interview.setOrganizationLatin(importedOrganization);
                interview.setProject(importedProject);
                interview.setInterviewNotes(importedInterviewNote);
                interview.setSalaryOffer(importedSalaryOffer);
                interview.setFinalOffer(importedOffer);
                interview.setGrade(importedGrade);
                interview.setJobLink(JOB_LINK);
                interview.setContact("-");
                interview.setComments("-");
                interview.setStatus(status);

                interview.setUser(user);
                interviewRepository.save(interview);
            }
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла", e);
        }
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
        dto.setEmail(interview.getUser().getEmail());
        return dto;
    }
}
