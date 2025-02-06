package ru.max.springboot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.max.springboot.service.UserService;

@Configuration
public class SecurityConfig {
    private final UserService userService; // сервис, с помощью которого тащим пользователя
    private final LoginSuccessHandler successUserHandler; // класс, в котором описана логика перенаправления пользователей по ролям

    public SecurityConfig(@Qualifier("userService") UserService userService, LoginSuccessHandler successUserHandler) {
        this.userService = userService;
        this.successUserHandler = successUserHandler;
    }

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // http.csrf().disable(); - попробуйте выяснить сами, что это даёт (отключение защиты от межсайтовой подделки запросов)
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin").hasAuthority("ROLE_ADMIN") // разрешаем входить на /admin пользователям с ролью Admin
                        .requestMatchers("/user").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN") // разрешаем входить на /user пользователям с ролью User and Admin
                        .requestMatchers("/login", "/registration").permitAll() // доступность всем
                        .anyRequest().authenticated()  // Spring сам подставит свою логин форму
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(successUserHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Необходимо для шифрования паролей
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
