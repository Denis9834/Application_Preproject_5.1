package ru.max.springboot.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.max.springboot.dto.UserDTO;
import ru.max.springboot.dto.UserResponseDTO;
import ru.max.springboot.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    List<UserResponseDTO> findAllUsers();

    User findUserById(Long id);

    Optional<User> findByName(String name);

    User findByEmail(String email);

    UserResponseDTO createUser(UserDTO userDto);

    User updateUser(Long id, UserDTO userDto);

    void deleteUser(Long id, User currentUser);

    User findByTelegramId(Long telegramId);

    User findByTelegramUserName(String telegramUserName);

    User save(User user);
}
