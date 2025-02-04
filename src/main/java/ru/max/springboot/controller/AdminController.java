package ru.max.springboot.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.model.User;
import ru.max.springboot.service.RoleService;
import ru.max.springboot.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    //Список пользователей
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/index";
    }

    //Удаление пользователя по id
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    //получение пользователя по id для редактирования
    @GetMapping("/edit/{userId}")
    public String editUserForm(@PathVariable Long userId, Model model) {
        model.addAttribute("user", userService.findUserById(userId));
        model.addAttribute("allRoles", roleService.findAllRoles());
        return "admin/edit";
    }

    //Редактирование пользователя
    @PostMapping("/edit")
    public String updateUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        if (!userService.updateUser(user.getId(), user, bindingResult) || bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.findAllRoles());
            return "admin/edit";
        }
        return "redirect:/admin";
    }
}
