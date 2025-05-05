package ru.max.springboot.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.max.springboot.dto.UserDTO;
import ru.max.springboot.dto.UserResponseDTO;
import ru.max.springboot.model.User;
import ru.max.springboot.service.impl.InterviewServiceImpl;
import ru.max.springboot.service.impl.RoleServiceImpl;
import ru.max.springboot.service.impl.UserServiceImpl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserServiceImpl userServiceImpl;
    private final RoleServiceImpl roleServiceImpl;
    private final InterviewServiceImpl interviewServiceImpl;

    @Autowired
    public AdminRestController(UserServiceImpl userServiceImpl, RoleServiceImpl roleServiceImpl, InterviewServiceImpl interviewServiceImpl) {
        this.userServiceImpl = userServiceImpl;
        this.roleServiceImpl = roleServiceImpl;
        this.interviewServiceImpl = interviewServiceImpl;
    }

    //Список пользователей
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> userResponseDTOS = userServiceImpl.findAllUsers();
        return ResponseEntity.ok(userResponseDTOS);
    }

    //Удаление пользователя по id
    @DeleteMapping("/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        userServiceImpl.deleteUser(id, currentUser);
        return ResponseEntity.ok("Пользователь успешно удален!");
    }

    //получение пользователя по id
    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userServiceImpl.findUserById(id));
    }

    //Редактирование пользователя
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody @Valid UserDTO userDto) {
        try {
            User updatedUser = userServiceImpl.updateUser(id, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    //Новый пользователь
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody @Valid UserDTO userDto) {
        try {
            UserResponseDTO createdUser = userServiceImpl.createUser(userDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл не выбран");
        }
        try {
            interviewServiceImpl.importFromExcel(file);
            return ResponseEntity.ok("Импорт завершён успешно");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
