package com.example.hr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Google Gemini AI Service â€” tÃ­ch há»£p LLM cho chatbot HR.
 * Chá»‰ active khi ai.gemini.enabled=true vÃ  cÃ³ API key.
 * API key miá»…n phÃ­: https://aistudio.google.com/app/apikey
 */
@Service
@ConditionalOnProperty(name = "ai.gemini.enabled", havingValue = "true")
public class GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${ai.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String endpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Gá»i Gemini API vá»›i system prompt + user message.
     * @param systemPrompt Context vá» HR system
     * @param userMessage CÃ¢u há»i cá»§a user
     * @return CÃ¢u tráº£ lá»i tá»« AI
     */
    public String chat(String systemPrompt, String userMessage) {
        try {
            String url = endpoint + "/" + model + ":generateContent?key=" + apiKey;

            // Build request body theo Gemini API format
            Map<String, Object> body = new LinkedHashMap<>();

            // System instruction
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                body.put("system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ));
            }

            // User message
            body.put("contents", List.of(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            )));

            // Generation config
            body.put("generationConfig", Map.of(
                    "temperature", 0.7,
                    "maxOutputTokens", 512,
                    "topP", 0.9
            ));

            // Safety settings â€” relax Ä‘á»ƒ khÃ´ng block HR content
            body.put("safetySettings", List.of(
                    Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_ONLY_HIGH"),
                    Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_ONLY_HIGH")
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractText(response.getBody());
            }
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Chat vá»›i conversation history (multi-turn).
     */
    public String chatWithHistory(String systemPrompt, List<Map<String, String>> history, String userMessage) {
        try {
            String url = endpoint + "/" + model + ":generateContent?key=" + apiKey;

            Map<String, Object> body = new LinkedHashMap<>();

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                body.put("system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ));
            }

            // Build contents from history
            List<Map<String, Object>> contents = new ArrayList<>();
            if (history != null) {
                for (Map<String, String> msg : history) {
                    contents.add(Map.of(
                            "role", msg.getOrDefault("role", "user"),
                            "parts", List.of(Map.of("text", msg.getOrDefault("content", "")))
                    ));
                }
            }
            // Add current message
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            ));
            body.put("contents", contents);
            body.put("generationConfig", Map.of("temperature", 0.7, "maxOutputTokens", 512));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> response = restTemplate.postForEntity(url,
                    new HttpEntity<>(body, headers), Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractText(response.getBody());
            }
        } catch (Exception e) {
            log.warn("Gemini multi-turn failed: {}", e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> responseBody) {
        try {
            List<?> candidates = (List<?>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                if (content != null) {
                    List<?> parts = (List<?>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<?, ?> part = (Map<?, ?>) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract Gemini response text: {}", e.getMessage());
        }
        return null;
    }

    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String getModel() {
        return model;
    }
}


