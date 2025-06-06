package ru.max.springboot.controller.telegram;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.model.User;
import ru.max.springboot.service.impl.RoleServiceImpl;
import ru.max.springboot.service.impl.UserServiceImpl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.max.springboot.config.SecurityConfig.passwordEncoder;

/**
 * Контроллер аутентификации через ТГ
 *
 * - Принимаются данные от Telegram WebApp
 * - Проверяется подлинность данных через алгоритм Telegram Login Widget
 * - Если пользователя с таким id нет - то создается новый
 * - выполняется авторизация пользователя
 */

@Slf4j
@RestController
@RequestMapping("/api/auth/telegram")
public class AuthTelegramController {

    private final UserServiceImpl userServiceImpl;
    private final RoleServiceImpl roleServiceImpl;
    @Value("${telegram.bot.token}")
    private String tgBotToken;

    public AuthTelegramController(UserServiceImpl userServiceImpl, RoleServiceImpl roleServiceImpl) {
        this.userServiceImpl = userServiceImpl;
        this.roleServiceImpl = roleServiceImpl;
    }

    @PostMapping
    public ResponseEntity<String> authenticate(@RequestBody Map<String, String> telegramData,
                                               HttpServletRequest request) {
        if (!telegramDataIsValid(telegramData)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Недействительная подпись");
        }

        String telegramUsername = telegramData.get("username");
        Long telegramId = Long.valueOf(telegramData.get("id"));
        User user = userServiceImpl.findByTelegramId(telegramId);

        if (user == null) {
            user = new User();
            user.setTelegramUsername(telegramUsername);
            String name = telegramData.get("first_name")
                    + (telegramData.get("last_name") != null
                    ? " " + telegramData.get("last_name")
                    : "");
            user.setName(name);
            user.setEmail(telegramId + "@telegram.ru");
            user.setAge(18);
            user.setRoles(new HashSet<>(Set.of(roleServiceImpl.findByRole("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Роль USER не существует")))));
            user.setPassword(passwordEncoder().encode(UUID.randomUUID().toString()));
            user.setTelegramId(Long.valueOf(telegramData.get("id")));
            user = userServiceImpl.save(user);
        }

        /*дополнительная проверка если у User активная подписка на Boosty*/
        userServiceImpl.validateBoostyAccess(user.getEmail(), user.getTelegramId());

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext()
        );
        return ResponseEntity.ok(user.getEmail());
    }

    private boolean telegramDataIsValid(Map<String, String> data) {
        String hash = data.remove("hash");
        StringBuilder sb = new StringBuilder();
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append(e.getKey()).append("=").append(e.getValue()).append("\n"));
        sb.deleteCharAt(sb.length() - 1);
        String dataCheckString = sb.toString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(tgBotToken.getBytes(UTF_8));

            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] hmacBytes = hmac.doFinal(dataCheckString.getBytes(UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hmacBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equals(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
