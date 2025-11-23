package com.newsportal.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for job queues
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "newsportal.exchange";

    public static final String NEWS_REWRITE_QUEUE = "news_rewrite";
    public static final String IMAGE_GENERATION_QUEUE = "image_generation";
    public static final String SOCIAL_CARD_QUEUE = "social_card_generation";

    public static final String REWRITE_ROUTING_KEY = "news.rewrite";
    public static final String IMAGE_GEN_ROUTING_KEY = "news.image.generate";
    public static final String SOCIAL_CARD_ROUTING_KEY = "news.social.card";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue newsRewriteQueue() {
        return QueueBuilder.durable(NEWS_REWRITE_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue imageGenerationQueue() {
        return QueueBuilder.durable(IMAGE_GENERATION_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Queue socialCardQueue() {
        return QueueBuilder.durable(SOCIAL_CARD_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .build();
    }

    @Bean
    public Binding newsRewriteBinding(Queue newsRewriteQueue, TopicExchange exchange) {
        return BindingBuilder.bind(newsRewriteQueue)
                .to(exchange)
                .with(REWRITE_ROUTING_KEY);
    }

    @Bean
    public Binding imageGenerationBinding(Queue imageGenerationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(imageGenerationQueue)
                .to(exchange)
                .with(IMAGE_GEN_ROUTING_KEY);
    }

    @Bean
    public Binding socialCardBinding(Queue socialCardQueue, TopicExchange exchange) {
        return BindingBuilder.bind(socialCardQueue)
                .to(exchange)
                .with(SOCIAL_CARD_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
