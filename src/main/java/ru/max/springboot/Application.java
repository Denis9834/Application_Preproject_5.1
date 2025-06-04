package ru.max.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.max.springboot.dto.boosty.BoostySession;
import ru.max.springboot.service.boosty.BoostyTokenService;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
