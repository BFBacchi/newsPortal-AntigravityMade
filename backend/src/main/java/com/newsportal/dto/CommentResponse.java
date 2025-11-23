package com.newsportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long newsId;
    private String username;
    private String content;
    private Long parentId;

    @Builder.Default
    private List<CommentResponse> replies = new ArrayList<>();

    private Boolean moderated;
    private Boolean approved;
    private Integer reportedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
