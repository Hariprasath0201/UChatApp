package com.example.chatapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate = new RestTemplate();

    // FIX: this was hardcoded before, so the translation.api.url property in
    // application.properties had zero effect. Now it's actually read from config.
    @Value("${translation.api.url:http://localhost:5000/translate}")
    private String translationApiUrl;

    public String translate(String text, String from, String to) {
        if (from == null || to == null || from.equalsIgnoreCase(to)) return text;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                    "q", text,
                    "source", from.toLowerCase(),
                    "target", to.toLowerCase(),
                    "format", "text",
                    "api_key", ""
            );

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
            Map<String, Object> response = restTemplate.postForObject(translationApiUrl, entity, Map.class);

            if (response != null && response.containsKey("translatedText")) {
                return response.get("translatedText").toString();
            }

            throw new RuntimeException("Empty response from Translation API");

        } catch (Exception e) {
            System.err.println("Translation API error: " + e.getMessage() + ". Using fallback mock translation.");
            return String.format("[%s to %s]: %s", from.toUpperCase(), to.toUpperCase(), text);
        }
    }
}
