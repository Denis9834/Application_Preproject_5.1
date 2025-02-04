package ru.max.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import ru.max.springboot.model.Role;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.RoleRepository;
import ru.max.springboot.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Список всех пользователей
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    //поиск пользователя по его id
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    //Поиск пользователя по имени
    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    // Проверка имени пользователя на уникальность
    public boolean isUsernameUnique(String name) {
        return !findByName(name).isPresent();
    }

    //Создание нового пользователя (по умолчанию с ролью User)
    @Transactional
    public boolean createUser(User user) {
        if (!isUsernameUnique(user.getUsername())) {
            return false; //Имя занято
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // шифрование пароля

        Role defaultUserRole = roleRepository.findByRole("ROLE_USER"); //при регистрации по default ROLE_USER
        Set<Role> roles = new HashSet<>();
        roles.add(defaultUserRole);
        user.setRoles(roles);

        userRepository.save(user);
        return true;
    }

    //удаление пользователя с БД
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    //обновление пользователя
    @Transactional
    public boolean updateUser(long id, User updateUser, BindingResult bindingResult) {
        User userToBeUpdated = findUserById(id);

        // проверка на уникальность имени пользователя
        if (!userToBeUpdated.getName().equals(updateUser.getName()) &&
                userRepository.findByName(updateUser.getName()).isPresent()) {
            bindingResult.rejectValue(
                    "name", "error.user", "Пользователь с таким именем уже существует");
            return false;
        }

        userToBeUpdated.setName(updateUser.getName());

        //для шифрования нового пароля
        if (updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()) {
            userToBeUpdated.setPassword(passwordEncoder.encode(updateUser.getPassword()));
        }

        userToBeUpdated.setRoles(updateUser.getRoles());
        userRepository.save(userToBeUpdated);
        return true;
    }
}
