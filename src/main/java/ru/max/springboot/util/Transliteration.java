package ru.max.springboot.util;

import com.ibm.icu.text.Transliterator;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class Transliteration {
    private static final String CYRILLIC_TO_LATIN = "Russian-Latin/BGN";
    private final Transliterator transliterator;

    public Transliteration() {
        this.transliterator = Transliterator.getInstance(CYRILLIC_TO_LATIN);
    }

    @Named("toLatin")
    public String toLatin(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return transliterator.transliterate(text);
    }
}
