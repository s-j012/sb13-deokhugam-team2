package com.deokhugam.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.deokhugammission.comment.entity.Comment;
import com.codeit.deokhugammission.comment.repository.CommentRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@DataJpaTest
@EntityScan(basePackageClasses = Comment.class)
@EnableJpaRepositories(basePackageClasses = CommentRepository.class)
@Import(CommentRepositoryTest.JpaAuditingTestConfig.class)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @TestConfiguration
    @EnableJpaAuditing
    static class JpaAuditingTestConfig {
    }

    @Test
    @DisplayName("댓글을 물리 삭제하면 DB에서 실제로 제거된다")
    void hardDeleteComment() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment = new Comment(
                "물리 삭제 테스트 댓글",
                userId,
                reviewId
        );

        Comment savedComment =
                commentRepository.saveAndFlush(comment);

        UUID commentId = savedComment.getId();

        // when
        commentRepository.delete(savedComment);
        commentRepository.flush();

        entityManager.clear();

        // then
        assertThat(
                commentRepository.findById(commentId)
        ).isEmpty();
    }

    @Test
    @DisplayName("논리 삭제된 댓글은 일반 목록 조회에서 제외된다")
    void softDeletedCommentIsExcludedFromList() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment activeComment = new Comment(
                "정상 댓글",
                userId,
                reviewId
        );

        Comment deletedComment = new Comment(
                "삭제된 댓글",
                userId,
                reviewId
        );

        deletedComment.softDelete();

        commentRepository.save(activeComment);
        commentRepository.save(deletedComment);
        commentRepository.flush();

        entityManager.clear();

        // when
        List<Comment> comments =
                commentRepository
                        .findByReviewIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(
                                reviewId,
                                PageRequest.of(0, 10)
                        );

        // then
        assertThat(comments).hasSize(1);

        assertThat(comments.get(0).getContent())
                .isEqualTo("정상 댓글");
    }

    @Test
    @DisplayName("댓글 목록은 생성 시간 순으로 조회된다")
    void commentsAreSortedByCreatedAt() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment firstComment = new Comment(
                "첫 번째 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(firstComment);

        Thread.sleep(100);

        Comment secondComment = new Comment(
                "두 번째 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(secondComment);

        entityManager.clear();

        // when
        List<Comment> comments =
                commentRepository
                        .findByReviewIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(
                                reviewId,
                                PageRequest.of(0, 10)
                        );

        // then
        assertThat(comments).hasSize(2);

        assertThat(comments.get(0).getContent())
                .isEqualTo("첫 번째 댓글");

        assertThat(comments.get(1).getContent())
                .isEqualTo("두 번째 댓글");
    }

    @Test
    @DisplayName("after 이후에 생성된 댓글만 조회된다")
    void findCommentsAfterCreatedAt() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment firstComment = new Comment(
                "이전 댓글",
                userId,
                reviewId
        );

        Comment savedFirstComment =
                commentRepository.saveAndFlush(firstComment);

        UUID firstCommentId = savedFirstComment.getId();

        /*
         * DB의 TIMESTAMP 정밀도와 동일한 값을 사용하기 위해
         * 영속성 컨텍스트를 비운 후 DB에서 다시 조회한다.
         */
        entityManager.clear();

        Comment reloadedFirstComment =
                commentRepository.findById(firstCommentId)
                        .orElseThrow();

        LocalDateTime after =
                reloadedFirstComment.getCreatedAt();

        Thread.sleep(1000);

        Comment secondComment = new Comment(
                "이후 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(secondComment);

        entityManager.clear();

        // when
        List<Comment> comments =
                commentRepository
                        .findByReviewIdAndDeletedAtIsNullAndCreatedAtAfterOrderByCreatedAtAscIdAsc(
                                reviewId,
                                after,
                                PageRequest.of(0, 10)
                        );

        // then
        assertThat(comments)
                .extracting(Comment::getContent)
                .containsExactly("이후 댓글");
    }

    @Test
    @DisplayName("limit만큼 댓글이 조회된다")
    void commentsAreLimitedByPageSize() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        for (int i = 1; i <= 5; i++) {

            Comment comment = new Comment(
                    "댓글 " + i,
                    userId,
                    reviewId
            );

            commentRepository.save(comment);
        }

        commentRepository.flush();

        entityManager.clear();

        // when
        List<Comment> comments =
                commentRepository
                        .findByReviewIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(
                                reviewId,
                                PageRequest.of(0, 3)
                        );

        // then
        assertThat(comments).hasSize(3);
    }
}