package ru.max.springboot.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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
    public String listUsers(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("user", new User()); // Добавляем текущего пользователя
        model.addAttribute("allRoles", roleService.findAllRoles());
        return "admin/index";
    }

    //Удаление пользователя по id
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, @AuthenticationPrincipal User currentUser, RedirectAttributes redirectAttributes) {
        if (id.equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Нельзя удалить самого себя");
            return "redirect:/admin";
        }
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    //получение пользователя по id для редактирования
    @GetMapping("/edit/{userId}")
    public String editUserForm(@PathVariable Long userId, Model model) {
        model.addAttribute("user", userService.findUserById(userId));
        model.addAttribute("allRoles", roleService.findAllRoles());
        return "admin/index";
    }

    //Редактирование пользователя
    @PostMapping("/edit")
    public String updateUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", roleService.findAllRoles());
            model.addAttribute("editModelOpen", true);
            return "admin/index";
        }

        //Если пароль не менялся
        User existingPass = userService.findUserById(user.getId());
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingPass.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userService.updateUser(user.getId(), user, bindingResult);
        return "redirect:/admin";
    }

    //Новый пользователь
    @PostMapping("/add")
    public String addUser (@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors() || !userService.createUser(user, bindingResult)) {
            model.addAttribute("allRoles", roleService.findAllRoles());
            model.addAttribute("activeTab", "addUser");
            return "admin/index";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Пользователь успешно добавлен!");
        return "redirect:/admin";
    }
}
