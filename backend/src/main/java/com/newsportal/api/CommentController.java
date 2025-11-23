package com.newsportal.api;

import com.newsportal.dto.CommentRequest;
import com.newsportal.dto.CommentResponse;
import com.newsportal.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news/{newsId}/comments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long newsId) {
        List<CommentResponse> comments = commentService.getCommentsByNewsId(newsId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long newsId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse comment = commentService.createComment(newsId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportComment(@PathVariable Long commentId) {
        commentService.reportComment(commentId);
        return ResponseEntity.ok().build();
    }
}
