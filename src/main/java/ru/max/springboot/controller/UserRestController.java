package ru.max.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.model.User;
import ru.max.springboot.service.UserService;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    //страница пользователя
    @GetMapping("/user")
    public ResponseEntity<User> getUserForm(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
}
