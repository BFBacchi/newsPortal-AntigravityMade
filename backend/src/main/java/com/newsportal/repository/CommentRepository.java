package com.newsportal.repository;

import com.newsportal.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByNewsIdAndParentIsNull(Long newsId, Pageable pageable);

    List<Comment> findByNewsIdAndParentIsNullOrderByCreatedAtDesc(Long newsId);

    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    @Query("SELECT c FROM Comment c WHERE c.moderated = false ORDER BY c.reportedCount DESC, c.createdAt ASC")
    Page<Comment> findUnmoderatedComments(Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.reportedCount >= :threshold ORDER BY c.reportedCount DESC")
    Page<Comment> findReportedComments(@Param("threshold") int threshold, Pageable pageable);

    long countByNewsIdAndApprovedTrue(Long newsId);
}
