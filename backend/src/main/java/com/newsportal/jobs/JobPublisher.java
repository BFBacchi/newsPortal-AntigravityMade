package com.newsportal.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for publishing jobs to RabbitMQ
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "newsportal.exchange";
    private static final String REWRITE_ROUTING_KEY = "news.rewrite";
    private static final String IMAGE_GEN_ROUTING_KEY = "news.image.generate";
    private static final String SOCIAL_CARD_ROUTING_KEY = "news.social.card";

    /**
     * Publish news rewrite job
     */
    public void publishNewsRewriteJob(Long newsId) {
        log.info("Publishing news rewrite job for ID: {}", newsId);

        Map<String, Object> message = new HashMap<>();
        message.put("newsId", newsId);
        message.put("timestamp", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(EXCHANGE, REWRITE_ROUTING_KEY, message);
    }

    /**
     * Publish image generation job
     */
    public void publishImageGenerationJob(Long newsId) {
        log.info("Publishing image generation job for ID: {}", newsId);

        Map<String, Object> message = new HashMap<>();
        message.put("newsId", newsId);
        message.put("timestamp", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(EXCHANGE, IMAGE_GEN_ROUTING_KEY, message);
    }

    /**
     * Publish social card generation job
     */
    public void publishSocialCardJob(Long newsId) {
        log.info("Publishing social card generation job for ID: {}", newsId);

        Map<String, Object> message = new HashMap<>();
        message.put("newsId", newsId);
        message.put("timestamp", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(EXCHANGE, SOCIAL_CARD_ROUTING_KEY, message);
    }

    /**
     * Publish complete pipeline job (rewrite -> image -> social card)
     */
    public void publishCompletePipeline(Long newsId) {
        log.info("Publishing complete pipeline for news ID: {}", newsId);

        publishNewsRewriteJob(newsId);
        publishImageGenerationJob(newsId);
        publishSocialCardJob(newsId);
    }
}
