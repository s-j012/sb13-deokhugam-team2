package com.codeit.deokhugammission.comment.repository;

import com.codeit.deokhugammission.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByReviewIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(
            UUID reviewId,
            Pageable pageable
    );

    List<Comment> findByReviewIdAndDeletedAtIsNullAndCreatedAtAfterOrderByCreatedAtAscIdAsc(
            UUID reviewId,
            LocalDateTime after,
            Pageable pageable
    );
}