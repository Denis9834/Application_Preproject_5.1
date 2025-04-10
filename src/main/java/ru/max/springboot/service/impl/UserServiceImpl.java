package ru.max.springboot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.max.springboot.dto.UserDTO;
import ru.max.springboot.dto.UserResponseDTO;
import ru.max.springboot.model.Role;
import ru.max.springboot.model.User;
import ru.max.springboot.repository.RoleRepository;
import ru.max.springboot.repository.UserRepository;
import ru.max.springboot.service.UserService;

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

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                           RoleServiceImpl roleService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
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
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //Создание нового пользователя (по умолчанию с ролью User)
    @Override
    @Transactional
    public UserResponseDTO createUser(UserDTO userDto) {

        //проверка на уникальность email
        if (findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        //проверка на уникальность имени
        if (findByName(userDto.getName()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
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
                        .collect(Collectors.toList())
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

    //обновление пользователя
    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDto) {
        User userToBeUpdated = findUserById(id);

        // проверка на уникальность пользователя по имени
        if (!userToBeUpdated.getName().equals(userDto.getName()) &&
                userRepository.findByName(userDto.getName()).isPresent()) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

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

    //Авторизация пользователя name/email
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<User> userAuth = userRepository.findByEmail(username);

        if (userAuth.isEmpty()) {
            userAuth = userRepository.findByName(username);
        }
        return userAuth.orElseThrow(() ->
                new UsernameNotFoundException("Пользователь не найден: " + username));
    }
}
