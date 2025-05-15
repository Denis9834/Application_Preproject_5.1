package ru.max.springboot.service.impl;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.max.springboot.dto.InterviewDTO;
import ru.max.springboot.dto.InterviewResponseDTO;
import ru.max.springboot.mapper.InterviewMapper;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.max.springboot.config.SecurityConfig.passwordEncoder;

@Slf4j
@Service
@Transactional
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final Transliteration transliteration;
    private final InterviewMapper interviewMapper;

    @Autowired
    public InterviewServiceImpl(InterviewRepository interviewRepository, UserService userService,
                                RoleService roleService, Transliteration transliteration, InterviewMapper interviewMapper) {
        this.interviewRepository = interviewRepository;
        this.userService = userService;
        this.roleService = roleService;
        this.transliteration = transliteration;
        this.interviewMapper = interviewMapper;
    }

    //Получение собеседований для пользователя
    @Override
    public List<InterviewResponseDTO> getInterviewByUser(User user) {
        List<Interview> interviews = interviewRepository.findByUser(user);
        log.debug("Получено {} собеседований для пользователя id={}", interviews.size(), user.getId());
        return mapToDtoList(interviews);
    }

    //Получение всех собеседований у Admin
    public List<InterviewResponseDTO> getAllInterviews() {
        return mapToDtoList(interviewRepository.findAll());
    }

    //Создание нового собеседования
    @Override
    public Interview createInterview(InterviewDTO dto, User user) {
        Interview entity = interviewMapper.toEntity(dto, user);
        Interview saved = interviewRepository.save(entity);
        log.info("Создано собеседование: id={}, userId={}, email={}, organization={}",
                saved.getInterviewId(), user.getId(), user.getEmail(), saved.getOrganization());
        return saved;
    }

    //Редактирование собеседования
    @Override
    public Interview updateInterview(Long id, InterviewDTO dto, User user) {
        Interview interview = getByIdAndUser(id, user);
        interviewMapper.updateInterviewFromDto(dto, interview);
        Interview updated = interviewRepository.save(interview);
        log.info("Обновлено собеседование: id={}, userId={}, email={}, organization={}",
                updated.getInterviewId(), user.getId(), user.getEmail(), updated.getOrganization());
        return updated;
    }

    // получение собеседования по id
    @Override
    public Interview getByIdAndUser(Long id, User user) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Собеседование не найдено"));
    }

    //Удаление собеседования
    @Override
    public void deleteInterview(Long id, User user) {
        Interview interview = getByIdAndUser(id, user);
        interviewRepository.delete(interview);
        log.warn("Удалено собеседование: id={}, userId={}", id, user.getId());
    }

    //обновление статуса на "Пройдено"
    @Override
    public void updateStatusToPassed(Long id, String notes) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Собеседование не найдено"));

        interview.setStatus(InterviewStatus.PASSED);
        interview.setInterviewNotes(notes);
        log.info("Обновлён статус собеседования на 'Пройдено': id={}, notes={}", id, notes);
    }

    //обновление статуса на "Оффер"
    @Override
    public void updateStatusToOffered(Long id, BigDecimal offer) {
        Interview interview = interviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Собеседование не найдено"));

        interview.setStatus(InterviewStatus.OFFERED);
        interview.setFinalOffer(offer);
        log.info("Обновлён статус собеседования на 'Оффер': id={}, offer={}", id, offer);

    }

    //Неточный поиск у Admin
    @Override
    public List<InterviewResponseDTO> searchFuzzyAllByOrganization(String term) {
        String termUpperCase = term.toUpperCase();
        String termLatinUpperCase = transliteration.toLatin(term).toUpperCase();
        return mapToDtoList(interviewRepository.searchFuzzyAllByOrganization(termUpperCase, termLatinUpperCase));
    }

    //Неточный поиск у User
    @Override
    public List<InterviewResponseDTO> searchFuzzyByUserAllByOrganization(String term, User user) {
        String termUpperCase = term.toUpperCase();
        String termLatinUpperCase = transliteration.toLatin(term).toUpperCase();
        return mapToDtoList(interviewRepository.searchFuzzyByUserAllByOrganization(user.getId(), termUpperCase, termLatinUpperCase));
    }

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

                String email = formatter.formatCellValue(row.getCell(1)).trim();
                if (email.isEmpty()) {
                    continue;
                }

                InterviewStatus status = parseStatus(formatter.formatCellValue(row.getCell(8)));
                User user = resolveOrCreateUser(email);
                InterviewDTO dto = parseRowToDto(row, status, formatter);

                Interview interview = interviewMapper.toEntity(dto, user);
                interviewRepository.save(interview);
            }
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла", e);
        }
    }

    // Создание пользователя после парсинга
    private User resolveOrCreateUser(String email) {
        return Optional.ofNullable(userService.findByEmail(email))
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("-");
                    u.setEmail(email);
                    u.setPassword(passwordEncoder().encode(UUID.randomUUID().toString()));
                    Role userRole = roleService.findByRole("ROLE_USER")
                            .orElseThrow(() -> new RuntimeException("Ошибка при назначении роли пользователю"));
                    u.setRoles(Set.of(userRole));
                    u.setAge(18);
                    return userService.save(u);
                });
    }

    // Парсинг статуса
    private InterviewStatus parseStatus(String rawStatus) {
        if (rawStatus == null) return InterviewStatus.PASSED;
        return switch (rawStatus.trim().toLowerCase()) {
            case "offer", "apply" -> InterviewStatus.OFFERED;
            default -> InterviewStatus.PASSED;
        };
    }

    //Парсинг по рядам
    private InterviewDTO parseRowToDto(Row row, InterviewStatus status, DataFormatter formatter) {
        InterviewDTO dto = new InterviewDTO();

        dto.setDataTime(row.getCell(0).getLocalDateTimeCellValue());
        dto.setOrganization(formatter.formatCellValue(row.getCell(2)).trim());
        dto.setProject(formatter.formatCellValue(row.getCell(3)).trim());
        dto.setInterviewNotes(formatter.formatCellValue(row.getCell(5)).trim());
        // парсинг 6 столба как строку, даже если в Excel она числовая
        String salaryText = formatter.formatCellValue(row.getCell(6)).trim();

        if (!salaryText.isEmpty()) {
            try {
                dto.setSalaryOffer(new BigDecimal(salaryText));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Неверный формат: " + salaryText, ex);
            }
        }

        dto.setFinalOffer(status == InterviewStatus.OFFERED ? dto.getSalaryOffer() : null);
        dto.setGrade(formatter.formatCellValue(row.getCell(7)).trim());
        dto.setJobLink("https://hh.ru");
        dto.setContact("-");
        dto.setComments("-");
        dto.setStatus(status);

        return dto;
    }

    private List<InterviewResponseDTO> mapToDtoList(List<Interview> interviews) {
        return interviews.stream()
                .map(interviewMapper::toDto)
                .collect(Collectors.toList());
    }
}
