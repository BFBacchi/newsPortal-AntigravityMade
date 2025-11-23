package com.newsportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with LLM APIs (OpenAI, Anthropic, etc.)
 * Handles news rewriting and content generation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LLMService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final AuditLogService auditLogService;

    @Value("${app.ai.llm.provider:openai}")
    private String provider;

    @Value("${app.ai.llm.openai-api-key:}")
    private String openaiApiKey;

    @Value("${app.ai.llm.anthropic-api-key:}")
    private String anthropicApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

    /**
     * Rewrite news article using LLM
     */
    public RewriteResult rewriteArticle(String sourceText, String sourceName, String sourceUrl) {
        log.info("Rewriting article from source: {}", sourceName);

        String prompt = buildRewritePrompt(sourceText, sourceName, sourceUrl);

        try {
            String response = callLLM(prompt);
            RewriteResult result = parseRewriteResponse(response);

            // Log to audit
            auditLogService.logLLMRewrite(sourceName, sourceUrl, prompt, response);

            return result;
        } catch (Exception e) {
            log.error("Error rewriting article: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to rewrite article", e);
        }
    }

    /**
     * Generate image prompt from article content
     */
    public String generateImagePrompt(String title, String excerpt) {
        log.info("Generating image prompt for: {}", title);

        String prompt = String.format(
                "Genera un prompt para crear una imagen editorial relacionada con el siguiente artículo. " +
                        "Estilo: moderno, tecnológico, luces brillantes, high-contrast, composición para hero image de web (landscape). "
                        +
                        "No incluir personas reconocibles. Devuelve SOLO el prompt, sin explicaciones adicionales.\n\n"
                        +
                        "Título: %s\n" +
                        "Resumen: %s\n\n" +
                        "Prompt para imagen:",
                title, excerpt);

        try {
            return callLLM(prompt);
        } catch (Exception e) {
            log.error("Error generating image prompt: {}", e.getMessage(), e);
            return "Modern tech news illustration with bright lights and high contrast";
        }
    }

    /**
     * Build the rewrite prompt template
     */
    private String buildRewritePrompt(String sourceText, String sourceName, String sourceUrl) {
        return String.format(
                """
                        Eres un redactor profesional de noticias. Toma el siguiente texto fuente y genera un nuevo artículo en español neutro,
                        estilo periodístico, 3 párrafos cortos y un título llamativo de máximo 80 caracteres.
                        Mantén los hechos verificables; si hay afirmaciones no verificables, marca con [NO_VERIFICADO].
                        No inventes citas. Incluye un resumen (excerpt) de 160 caracteres y 3 tags sugeridos.

                        SOURCE_NAME: %s
                        SOURCE_URL: %s
                        SOURCE_TEXT:
                        \"\"\"
                        %s
                        \"\"\"

                        INSTRUCCIONES:
                        - Conserva hechos comprobables (fechas, nombres, cifras) exactamente como están.
                        - Cambia la redacción para evitar plagio directo: reestructura oraciones, usa sinónimos y cambia el orden informativo.
                        - Si el contenido es opinión, etiquétalo como "opinion" en tags.
                        - Devuelve JSON válido con campos: title, excerpt, body_html, tags[], warnings[]

                        Responde SOLO con el JSON, sin texto adicional:
                        """,
                sourceName, sourceUrl, sourceText);
    }

    /**
     * Call the configured LLM provider
     */
    private String callLLM(String prompt) {
        if ("openai".equalsIgnoreCase(provider)) {
            return callOpenAI(prompt);
        } else if ("anthropic".equalsIgnoreCase(provider)) {
            return callAnthropic(prompt);
        } else {
            throw new IllegalStateException("Unsupported LLM provider: " + provider);
        }
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAI(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4-turbo-preview");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

        WebClient webClient = webClientBuilder
                .baseUrl(OPENAI_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("OpenAI API call failed", e);
        }
    }

    /**
     * Call Anthropic API
     */
    private String callAnthropic(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "claude-3-opus-20240229");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)));
        requestBody.put("max_tokens", 2000);

        WebClient webClient = webClientBuilder
                .baseUrl(ANTHROPIC_API_URL)
                .defaultHeader("x-api-key", anthropicApiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.get("content").get(0).get("text").asText();
        } catch (Exception e) {
            log.error("Anthropic API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Anthropic API call failed", e);
        }
    }

    /**
     * Parse the LLM response into structured data
     */
    private RewriteResult parseRewriteResponse(String response) {
        try {
            // Extract JSON from response (in case there's extra text)
            String jsonStr = response.trim();
            if (jsonStr.contains("```json")) {
                jsonStr = jsonStr.substring(jsonStr.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (jsonStr.contains("```")) {
                jsonStr = jsonStr.substring(jsonStr.indexOf("```") + 3);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            }

            JsonNode json = objectMapper.readTree(jsonStr.trim());

            RewriteResult result = new RewriteResult();
            result.setTitle(json.get("title").asText());
            result.setExcerpt(json.get("excerpt").asText());
            result.setBodyHtml(json.get("body_html").asText());

            // Parse tags
            JsonNode tagsNode = json.get("tags");
            if (tagsNode != null && tagsNode.isArray()) {
                result.setTags(objectMapper.convertValue(tagsNode, List.class));
            }

            // Parse warnings
            JsonNode warningsNode = json.get("warnings");
            if (warningsNode != null && warningsNode.isArray()) {
                result.setWarnings(objectMapper.convertValue(warningsNode, List.class));
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

    /**
     * Result of article rewriting
     */
    @lombok.Data
    public static class RewriteResult {
        private String title;
        private String excerpt;
        private String bodyHtml;
        private List<String> tags;
        private List<String> warnings;
    }
}
