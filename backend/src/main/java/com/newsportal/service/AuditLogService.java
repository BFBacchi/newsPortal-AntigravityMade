package com.newsportal.service;

import com.newsportal.model.AuditLog;
import com.newsportal.model.User;
import com.newsportal.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(String entityType, Long entityId, String action, User user, String metadata) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .user(user)
                .metadata(metadata)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional
    public void logContentChange(String entityType, Long entityId, String action, User user,
            String originalContent, String rewrittenContent,
            String llmPrompt, String llmModel) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .user(user)
                .originalContent(originalContent)
                .rewrittenContent(rewrittenContent)
                .llmPrompt(llmPrompt)
                .llmModel(llmModel)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional
    public void logLLMRewrite(String sourceName, String sourceUrl, String prompt, String response) {
        String metadata = String.format("Source: %s, URL: %s", sourceName, sourceUrl);
        AuditLog log = AuditLog.builder()
                .entityType("NEWS")
                .action("LLM_REWRITE")
                .llmPrompt(prompt)
                .rewrittenContent(response)
                .metadata(metadata)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional
    public void logImageGeneration(String newsId, String prompt, String imageUrl) {
        String metadata = String.format("NewsID: %s, ImageURL: %s", newsId, imageUrl);
        AuditLog log = AuditLog.builder()
                .entityType("NEWS")
                .entityId(Long.parseLong(newsId))
                .action("IMAGE_GENERATION")
                .llmPrompt(prompt)
                .metadata(metadata)
                .build();

        auditLogRepository.save(log);
    }
}
