package ru.max.springboot.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.max.springboot.model.Role;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.RoleRepository;
import ru.max.springboot.repository.UserRepository;
import ru.max.springboot.service.UserService;

@Controller
@RequestMapping("")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Форма для регистрации нового user
    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    //Обработка нового зарегистрированного пользователя
    @PostMapping("/registration")
    public String registrationUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        if (!userService.createUser(user)) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "registration";
        }
        return "redirect:/login";
    }

    //Форма логина
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }


}
