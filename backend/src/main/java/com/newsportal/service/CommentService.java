package com.newsportal.service;

import com.newsportal.dto.CommentRequest;
import com.newsportal.dto.CommentResponse;
import com.newsportal.model.Comment;
import com.newsportal.model.News;
import com.newsportal.model.User;
import com.newsportal.repository.CommentRepository;
import com.newsportal.repository.NewsRepository;
import com.newsportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByNewsId(Long newsId) {
        List<Comment> topLevelComments = commentRepository
                .findByNewsIdAndParentIsNullOrderByCreatedAtDesc(newsId);

        return topLevelComments.stream()
                .filter(Comment::getApproved)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(Long newsId, CommentRequest request) {
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + newsId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .news(news)
                .user(user)
                .content(request.getContent())
                .moderated(false)
                .approved(true) // Auto-approve for now, can be changed for moderation
                .reportedCount(0)
                .build();

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
        }

        comment = commentRepository.save(comment);

        auditLogService.logAction("Comment", comment.getId(), "CREATE", user,
                "Comment created on news: " + newsId);

        return convertToResponse(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only allow deletion by comment owner or admin
        if (!comment.getUser().getUsername().equals(username) &&
                !user.hasRole(User.Role.ROLE_ADMIN)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        commentRepository.delete(comment);

        auditLogService.logAction("Comment", id, "DELETE", user, "Comment deleted");
    }

    @Transactional
    public CommentResponse approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        comment.approve();
        comment = commentRepository.save(comment);

        auditLogService.logAction("Comment", id, "APPROVE", user, "Comment approved");

        return convertToResponse(comment);
    }

    @Transactional
    public CommentResponse rejectComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        comment.reject();
        comment = commentRepository.save(comment);

        auditLogService.logAction("Comment", id, "REJECT", user, "Comment rejected");

        return convertToResponse(comment);
    }

    @Transactional
    public void reportComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        comment.report();
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getUnmoderatedComments(Pageable pageable) {
        return commentRepository.findUnmoderatedComments(pageable)
                .map(this::convertToResponse);
    }

    private CommentResponse convertToResponse(Comment comment) {
        List<CommentResponse> replies = comment.getReplies().stream()
                .filter(Comment::getApproved)
                .map(this::convertToResponseWithoutReplies)
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .newsId(comment.getNews().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .replies(replies)
                .moderated(comment.getModerated())
                .approved(comment.getApproved())
                .reportedCount(comment.getReportedCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private CommentResponse convertToResponseWithoutReplies(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .newsId(comment.getNews().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .moderated(comment.getModerated())
                .approved(comment.getApproved())
                .reportedCount(comment.getReportedCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
