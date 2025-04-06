package ru.max.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.model.User;
import ru.max.springboot.service.impl.UserServiceImpl;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserServiceImpl userServiceImpl;

    @Autowired
    public UserRestController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    //страница пользователя
    @GetMapping("/user")
    public ResponseEntity<User> getUserForm(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
}
