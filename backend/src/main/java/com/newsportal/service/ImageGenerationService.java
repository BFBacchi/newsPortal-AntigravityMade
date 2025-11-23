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
import java.util.Map;

/**
 * Service for AI image generation (DALL-E, Stable Diffusion, etc.)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageGenerationService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final StorageService storageService;
    private final AuditLogService auditLogService;

    @Value("${app.ai.image.provider:openai}")
    private String provider;

    @Value("${app.ai.image.dalle-api-key:}")
    private String dalleApiKey;

    @Value("${app.ai.image.stability-api-key:}")
    private String stabilityApiKey;

    private static final String DALLE_API_URL = "https://api.openai.com/v1/images/generations";
    private static final String STABILITY_API_URL = "https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image";

    /**
     * Generate image from text prompt
     */
    public ImageGenerationResult generateImage(String prompt, String newsId) {
        log.info("Generating image for news ID: {} with prompt: {}", newsId, prompt);

        try {
            String imageUrl = callImageGenerationAPI(prompt);

            // Download and store the image
            String storedUrl = storageService.downloadAndStoreImage(imageUrl, newsId);

            // Log to audit
            auditLogService.logImageGeneration(newsId, prompt, imageUrl);

            ImageGenerationResult result = new ImageGenerationResult();
            result.setImageUrl(storedUrl);
            result.setPrompt(prompt);
            result.setProvider(provider);

            return result;
        } catch (Exception e) {
            log.error("Error generating image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate image", e);
        }
    }

    /**
     * Call the configured image generation provider
     */
    private String callImageGenerationAPI(String prompt) {
        if ("openai".equalsIgnoreCase(provider)) {
            return callDALLE(prompt);
        } else if ("stability".equalsIgnoreCase(provider)) {
            return callStabilityAI(prompt);
        } else {
            throw new IllegalStateException("Unsupported image generation provider: " + provider);
        }
    }

    /**
     * Call DALL-E API
     */
    private String callDALLE(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1792x1024"); // Landscape format
        requestBody.put("quality", "hd");
        requestBody.put("style", "vivid");

        WebClient webClient = webClientBuilder
                .baseUrl(DALLE_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + dalleApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.get("data").get(0).get("url").asText();
        } catch (Exception e) {
            log.error("DALL-E API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("DALL-E API call failed", e);
        }
    }

    /**
     * Call Stability AI API
     */
    private String callStabilityAI(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text_prompts", new Object[] {
                Map.of("text", prompt, "weight", 1)
        });
        requestBody.put("cfg_scale", 7);
        requestBody.put("height", 1024);
        requestBody.put("width", 1792);
        requestBody.put("samples", 1);
        requestBody.put("steps", 30);

        WebClient webClient = webClientBuilder
                .baseUrl(STABILITY_API_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + stabilityApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", "application/json")
                .build();

        try {
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode jsonResponse = objectMapper.readTree(response);
            String base64Image = jsonResponse.get("artifacts").get(0).get("base64").asText();

            // For Stability AI, we need to handle base64 differently
            // This is a simplified version - you'd need to decode and upload
            return "data:image/png;base64," + base64Image;
        } catch (Exception e) {
            log.error("Stability AI API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Stability AI API call failed", e);
        }
    }

    /**
     * Generate social media card/placa for a news article
     */
    public String generateSocialCard(String newsId, String title, String excerpt, String imageUrl) {
        log.info("Generating social card for news ID: {}", newsId);

        // This would typically use a headless browser (Puppeteer) or image manipulation
        // library
        // For now, we'll return a placeholder implementation
        // TODO: Implement actual social card generation

        return imageUrl; // Temporary: return the main image
    }

    /**
     * Result of image generation
     */
    @lombok.Data
    public static class ImageGenerationResult {
        private String imageUrl;
        private String prompt;
        private String provider;
    }
}
