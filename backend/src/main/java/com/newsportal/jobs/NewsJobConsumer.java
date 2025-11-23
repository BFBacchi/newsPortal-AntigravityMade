package com.newsportal.jobs;

import com.newsportal.model.News;
import com.newsportal.repository.NewsRepository;
import com.newsportal.service.ImageGenerationService;
import com.newsportal.service.LLMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer for processing news-related jobs from RabbitMQ
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NewsJobConsumer {

    private static final String NEWS_ID_KEY = "newsId";
    private static final String NEWS_NOT_FOUND_MSG = "News not found: ";

    private final NewsRepository newsRepository;
    private final LLMService llmService;
    private final ImageGenerationService imageGenerationService;

    /**
     * Process news rewriting job
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.news-rewrite:news_rewrite}")
    public void processNewsRewrite(Map<String, Object> message) {
        log.info("Processing news rewrite job: {}", message);

        try {
            Long newsId = Long.valueOf(message.get(NEWS_ID_KEY).toString());
            News news = newsRepository.findById(newsId)
                    .orElseThrow(() -> new RuntimeException(NEWS_NOT_FOUND_MSG + newsId));

            // Rewrite the article using LLM
            LLMService.RewriteResult result = llmService.rewriteArticle(
                    news.getBody(),
                    news.getAuthorSource(),
                    news.getUrlSource());

            // Update news with rewritten content
            news.setTitle(result.getTitle());
            news.setExcerpt(result.getExcerpt());
            news.setBody(result.getBodyHtml());

            if (result.getTags() != null && !result.getTags().isEmpty()) {
                news.setTagsFromList(result.getTags());
            }

            newsRepository.save(news);

            log.info("Successfully rewrote news ID: {}", newsId);

        } catch (Exception e) {
            log.error("Error processing news rewrite: {}", e.getMessage(), e);
            // TODO: Implement retry logic with exponential backoff
        }
    }

    /**
     * Process image generation job
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.image-generation:image_generation}")
    public void processImageGeneration(Map<String, Object> message) {
        log.info("Processing image generation job: {}", message);

        try {
            Long newsId = Long.valueOf(message.get(NEWS_ID_KEY).toString());
            News news = newsRepository.findById(newsId)
                    .orElseThrow(() -> new RuntimeException(NEWS_NOT_FOUND_MSG + newsId));

            // Generate image prompt from article
            String imagePrompt = llmService.generateImagePrompt(news.getTitle(), news.getExcerpt());

            // Generate image
            ImageGenerationService.ImageGenerationResult result = imageGenerationService.generateImage(imagePrompt,
                    newsId.toString());

            // Update news with generated image
            news.setPrimaryImageUrl(result.getImageUrl());
            newsRepository.save(news);

            log.info("Successfully generated image for news ID: {}", newsId);

        } catch (Exception e) {
            log.error("Error processing image generation: {}", e.getMessage(), e);
            // TODO: Implement retry logic with exponential backoff
        }
    }

    /**
     * Process social media card generation job
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.social-card:social_card_generation}")
    public void processSocialCardGeneration(Map<String, Object> message) {
        log.info("Processing social card generation job: {}", message);

        try {
            Long newsId = Long.valueOf(message.get(NEWS_ID_KEY).toString());
            News news = newsRepository.findById(newsId)
                    .orElseThrow(() -> new RuntimeException(NEWS_NOT_FOUND_MSG + newsId));

            // Generate social media card
            String socialCardUrl = imageGenerationService.generateSocialCard(
                    newsId.toString(),
                    news.getTitle(),
                    news.getExcerpt(),
                    news.getPrimaryImageUrl());

            log.info("Successfully generated social card for news ID: {} at URL: {}", newsId, socialCardUrl);

            // TODO: Store social card URL in MediaAsset and trigger social media posting

        } catch (Exception e) {
            log.error("Error processing social card generation: {}", e.getMessage(), e);
        }
    }
}
