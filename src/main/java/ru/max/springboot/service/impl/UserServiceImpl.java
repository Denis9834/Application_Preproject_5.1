package ru.max.springboot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.max.springboot.dto.UserDTO;
import ru.max.springboot.dto.UserResponseDTO;
import ru.max.springboot.dto.boosty.BoostySubscriberInfo;
import ru.max.springboot.model.Role;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.RoleRepository;
import ru.max.springboot.repository.UserRepository;
import ru.max.springboot.service.UserService;
import ru.max.springboot.service.boosty.BoostySubscriberService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleServiceImpl roleService;
    private final BoostySubscriberService boostySubscriberService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           RoleServiceImpl roleService, BoostySubscriberService boostySubscriberService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.boostySubscriberService = boostySubscriberService;
    }

    //Список всех пользователей
    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserResponseDTO)
                .collect(Collectors.toList());
    }

    //поиск пользователя по его id
    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    //Поиск пользователя по имени
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    //Поиск пользователя по email
    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    //Создание нового пользователя (по умолчанию с ролью User)
    @Override
    @Transactional
    public UserResponseDTO createUser(UserDTO userDto) {

        //проверка на уникальность email
        if (findByEmail(userDto.getEmail()) != null) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setAge(userDto.getAge());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Set<Role> roles = userDto.getRoleId().stream()
                .map(roleId -> roleService.getRoleById(roleId)
                        .orElseThrow(() -> new RuntimeException("Роль не найдена")))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        if (roles.isEmpty()) {
            roleRepository.findByRole("ROLE_USER").ifPresent(roles::add); //при регистрации по default ROLE_USER, если не указано вручную
        }

        user.setRoles(roles);

        User saveUser = userRepository.save(user);
        log.info("Пользователь {} успешно добавлен", userDto.getEmail());
        return convertToUserResponseDTO(saveUser);
    }

    //Преобразование User в UserResponseDTO
    public UserResponseDTO convertToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getRoles().stream()
                        .map(Role::getRole)
                        .collect(Collectors.toList()),
                user.getTelegramUsername()
        );
    }

    //удаление пользователя с БД
    @Override
    @Transactional
    public void deleteUser(Long id, User currentUser) {
        if (id.equals(currentUser.getId())) {
            throw new RuntimeException("Нельзя удалить самого себя!");
        }
        userRepository.deleteById(id);
    }

    //поиск пользователя по его telegramId
    @Override
    public User findByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId).orElse(null);
    }

    //поиск пользователя по его telegramUserName
    @Override
    public User findByTelegramUserName(String telegramUserName) {
        return userRepository.findByTelegramUsername(telegramUserName).orElse(null);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * - поиск пользователя по email и id
     * - получение информации о подписке
     * - если подписка активна - сохранить статус и дату след. оплаты
     */
    @Override
    public boolean validateBoostyAccess(String email, Long telegramId) {
        Optional<User> userOptional = userRepository.findByEmailAndTelegramId(email, telegramId);
        User user = userOptional.orElse(null);

        //проверка на активность подписки из локального кеша
        boolean validCache = user != null &&
                user.getBoostyNextPayTime() != null &&
                user.getBoostyNextPayTime().isAfter(LocalDateTime.now()) &&
                "active".equalsIgnoreCase(user.getBoostyStatus());

        if (validCache) {
            log.info("Подписка active из кэша: {}", user.getEmail());
            return true;
        }

        log.info("Проверка подписки через Boosty для email: {}", email);
        //запрос на бусти для информации о подписки
        BoostySubscriberInfo info = boostySubscriberService.fetchSubscriberInfo(email);

        if (info == null || !"active".equalsIgnoreCase(info.getStatus())) {
            log.info("Подписка не найдена или неактивна: {}", email);

            if (user != null) {
                user.setBoostyStatus("inactive");
                user.setBoostyNextPayTime(null);
                userRepository.save(user);
                log.info("Статус подписки пользователя {} обновлён на inactive", email);
            }
            return false;
        }

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setTelegramId(telegramId);
        }

        user.setBoostyStatus("active");
        user.setBoostyNextPayTime(info.getNextPayTime());

        boolean hasSubscriberRole = user.getRoles().stream()
                .anyMatch(r -> "ROLE_SUBSCRIBER".equalsIgnoreCase(r.getRole()));

        if (!hasSubscriberRole) {
            Optional<Role> role = roleRepository.findByRole("ROLE_SUBSCRIBER");
            if (role.isPresent()) {
                user.getRoles().add(role.get());
            }
        }

        userRepository.save(user);
        log.info("Данные пользователя обновлены по подписке Boosty: {}", user.getEmail());

        return true;
    }

    /**
     * обновление email, username (если с бусти пришли обновленные) и статуса подписки в бд
     */
    @Override
    @Transactional
    public void updateTelegramUserInfo(Long telegramId, String boostyEmail, String telegramUsername) {
        Optional<User> userOptional = userRepository.findByTelegramId(telegramId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            //обновление email
            user.setEmail(boostyEmail);
            log.info("Email пользователя обновлен: {}", telegramId);

            //обновление username пользователя с ТГ
            if (telegramUsername != null && !telegramUsername.equals(user.getTelegramUsername())) {
                user.setTelegramUsername(telegramUsername);
                log.info("Username пользователя обновлен: {}", telegramId);
            }

            //обновление статуса подписки
            BoostySubscriberInfo info = boostySubscriberService.fetchSubscriberInfo(boostyEmail);
            if (info != null && "active".equalsIgnoreCase(info.getStatus())) {
                user.setBoostyStatus(info.getStatus());
                user.setBoostyNextPayTime(info.getNextPayTime());

                Role role = roleRepository.findByRole("ROLE_SUBSCRIBER")
                        .orElseThrow(() -> new RuntimeException("Роль SUBSCRIBER не найдена"));
                user.getRoles().add(role);
            }
            userRepository.save(user);
        } else {
            log.warn("Пользователь с Telegram ID {} не найден", telegramId);
        }
    }

    //обновление пользователя
    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDto) {
        User userToBeUpdated = findUserById(id);

        // проверка на уникальность пользователя по email
        if (!userToBeUpdated.getEmail().equals(userDto.getEmail()) &&
                userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        userToBeUpdated.setName(userDto.getName());
        userToBeUpdated.setAge(userDto.getAge());
        userToBeUpdated.setEmail(userDto.getEmail());

        // Проверка на изменение пароля
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            userToBeUpdated.setPassword(passwordEncoder.encode(userDto.getPassword())); // шифруем новый пароль
        }

        if (userDto.getRoleId() == null || userDto.getRoleId().isEmpty()) {
            throw new IllegalArgumentException("Роль не может отсутствовать");
        }

        Set<Role> roles = userDto.getRoleId().stream()
                .map(roleService::getRoleById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
        userToBeUpdated.setRoles(roles);

        return userToBeUpdated;
    }

    //Авторизация пользователя email
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userAuth = userRepository.findByEmail(username);

        return userAuth.orElseThrow(() ->
                new UsernameNotFoundException("Пользователь не найден: " + username));
    }
}
