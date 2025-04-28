package ru.max.springboot.util;

import com.ibm.icu.text.Transliterator;

public final class Transliteration {
    private static final String CYRILLIC_TO_LATIN = "Russian-Latin/BGN";
    private static final Transliterator TO_LATIN = Transliterator.getInstance(CYRILLIC_TO_LATIN);

    public static String toLatin(String text) {
        return TO_LATIN.transliterate(text);
    }
}
