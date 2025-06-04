package ru.max.springboot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.max.springboot.dto.boosty.BoostySession;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Класс для хранения сессии Boosty
 * <p>
 * - Загружается сессия из файла
 * - Обновляется и сохраняется обратно в файл
 */
public class BoostySessionStorage {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DEFAULT_PATH = "secrets/boosty_session.json";
    private static String sessionPath = System.getenv().getOrDefault("BOOSTY_SESSION_PATH", DEFAULT_PATH);

    public static BoostySession load() throws IOException {
        return mapper.readValue(Files.readAllBytes(Paths.get(sessionPath)), BoostySession.class);
    }

    public static void save(BoostySession session) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(Paths.get(sessionPath).toFile(), session);
    }
}
