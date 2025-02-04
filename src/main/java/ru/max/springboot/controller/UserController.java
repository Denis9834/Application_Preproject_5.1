package ru.max.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.max.springboot.model.User;
import ru.max.springboot.service.UserService;

@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //страница пользователя
    @GetMapping("/user")
    public String userForm(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "user/index";
    }
}
