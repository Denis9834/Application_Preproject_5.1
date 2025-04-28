package ru.max.springboot.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ru.max.springboot.model.User;
import ru.max.springboot.service.impl.RoleServiceImpl;
import ru.max.springboot.service.impl.UserServiceImpl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static ru.max.springboot.config.SecurityConfig.passwordEncoder;

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        }

        Long telegramId = Long.valueOf(telegramData.get("id"));

        User user = userServiceImpl.findByTelegramId(telegramId);

        if (user == null) {
            user = new User();
            user.setTelegramId(telegramId);
            user.setName(telegramData.get("first_name") +
                    (telegramData.get("last_name") != null ? " " + telegramData.get("last_name") : " "));
            user.setEmail("tg_" + telegramId + "@telegram.ru");
            user.setAge(0);
            user.setRoles(Set.of(roleServiceImpl.findByRole("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Role not found: ROLE_USER"))));
            user.setPassword(passwordEncoder().encode(UUID.randomUUID().toString()));
            user = userServiceImpl.save(user);
        }

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext()
        );
        return ResponseEntity.ok("ok");
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
