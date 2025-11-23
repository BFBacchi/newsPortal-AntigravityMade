package com.newsportal.dto;

import com.newsportal.model.MediaAsset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAssetResponse {

    private Long id;
    private String url;
    private MediaAsset.MediaType type;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    private String altText;
    private String generationPrompt;
    private Boolean aiGenerated;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
