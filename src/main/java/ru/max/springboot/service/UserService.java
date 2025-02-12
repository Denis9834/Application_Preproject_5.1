package ru.max.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UserService implements UserDetailsService {

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

    //Поиск пользователя по email
    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    //Поиск пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Проверка email на уникальность
    public boolean isEmailUnique(String email) {
        return !findByEmail(email).isPresent();
    }

    // Проверка имени на уникальность
    public boolean isNameUnique(String name) {
        return !findByName(name).isPresent();
    }

    //Создание нового пользователя (по умолчанию с ролью User)
    @Transactional
    public boolean createUser(User user, BindingResult bindingResult) {
        //проверка на уникальность имени
        if (!isNameUnique(user.getName())) {
            bindingResult.rejectValue("name", "error.user",
                    "Пользователь с таким именем уже существует");
            return false;
        }

        //проверка на уникальность email
        if (!isEmailUnique(user.getEmail())) {
            bindingResult.rejectValue("email", "error.user",
                    "Пользователь с таким email уже существует");
            return false;
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

        // проверка на уникальность пользователя по имени
        if (!userToBeUpdated.getName().equals(updateUser.getName()) &&
                userRepository.findByName(updateUser.getName()).isPresent()) {
            bindingResult.rejectValue(
                    "name", "error.user", "Пользователь с таким именем уже существует");
            return false;
        }

        // проверка на уникальность пользователя по email
        if (!userToBeUpdated.getEmail().equals(updateUser.getEmail()) &&
                userRepository.findByEmail(updateUser.getEmail()).isPresent()) {
            bindingResult.rejectValue(
                    "email", "error.user", "Пользователь с таким email уже существует");
            return false;
        }

        userToBeUpdated.setName(updateUser.getName());
        userToBeUpdated.setAge(updateUser.getAge());
        userToBeUpdated.setEmail(updateUser.getEmail());

        //для шифрования нового пароля
        if (updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()) {
            userToBeUpdated.setPassword(passwordEncoder.encode(updateUser.getPassword()));
        }

        userToBeUpdated.setRoles(updateUser.getRoles());
        userRepository.save(userToBeUpdated);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        return user.get();
    }
}
