package com.lkjmcsmp.domain;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageServiceTest {
    @Test
    void fallsBackToEnglishAndThenKey() {
        MessageService service = new MessageService(null, registry(), Map.of(
                "en", Map.of("greeting", "Hello {name}"),
                "ja", Map.of()));

        assertEquals("Hello Alex", service.get("ja", "greeting", "name", "Alex"));
        assertEquals("missing.key", service.get("ja", "missing.key"));
    }

    @Test
    void bundledEnglishAndJapaneseCatalogsHaveSameKeys() throws Exception {
        Map<String, String> english = load("lang/en.json");
        Map<String, String> japanese = load("lang/ja.json");

        assertEquals(english.keySet(), japanese.keySet());
    }

    private static MessageService.LanguageRegistry registry() {
        return new MessageService.LanguageRegistry("en", Map.of("en", "English", "ja", "日本語"));
    }

    private static Map<String, String> load(String path) throws Exception {
        try (var stream = MessageServiceTest.class.getClassLoader().getResourceAsStream(path);
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() { }.getType());
        }
    }
}
