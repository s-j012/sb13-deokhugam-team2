package com.deokhugam.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.entity.Comment;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
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

        commentRepository.delete(savedComment);
        commentRepository.flush();

        entityManager.clear();

        assertThat(
                commentRepository.findById(commentId)
        ).isEmpty();
    }

    @Test
    @DisplayName("논리 삭제된 댓글은 목록 조회에서 제외된다")
    void softDeletedCommentIsExcludedFromList() {

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

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        assertThat(comments)
                .hasSize(1);

        assertThat(comments.get(0).getContent())
                .isEqualTo("정상 댓글");
    }

    @Test
    @DisplayName("댓글 목록은 기본적으로 최신순으로 조회된다")
    void commentsAreSortedByCreatedAtDesc() throws Exception {

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

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        assertThat(comments).hasSize(2);

        assertThat(comments.get(0).getContent())
                .isEqualTo("두 번째 댓글");

        assertThat(comments.get(1).getContent())
                .isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("ASC 방향으로 댓글을 조회할 수 있다")
    void commentsAreSortedByCreatedAtAsc() throws Exception {

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

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "ASC",
                        null,
                        null,
                        10
                );

        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        assertThat(comments).hasSize(2);

        assertThat(comments.get(0).getContent())
                .isEqualTo("첫 번째 댓글");

        assertThat(comments.get(1).getContent())
                .isEqualTo("두 번째 댓글");
    }

    @Test
    @DisplayName("cursor 이후의 댓글을 조회할 수 있다")
    void findCommentsByCursor() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment firstComment = new Comment(
                "이전 댓글",
                userId,
                reviewId
        );

        Comment savedFirstComment =
                commentRepository.saveAndFlush(firstComment);

        UUID firstCommentId =
                savedFirstComment.getId();

        entityManager.clear();

        Comment reloadedFirstComment =
                commentRepository
                        .findById(firstCommentId)
                        .orElseThrow();

        LocalDateTime firstCreatedAt =
                reloadedFirstComment.getCreatedAt();

        Thread.sleep(1000);

        Comment secondComment = new Comment(
                "이후 댓글",
                userId,
                reviewId
        );

        commentRepository.saveAndFlush(secondComment);

        entityManager.clear();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "ASC",
                        firstCreatedAt.toString(),
                        firstCreatedAt,
                        10
                );

        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        assertThat(comments)
                .extracting(Comment::getContent)
                .containsExactly("이후 댓글");
    }

    @Test
    @DisplayName("limit + 1개의 댓글을 조회해 다음 페이지 여부를 판단할 수 있다")
    void findLimitPlusOneComments() {

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

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        3
                );

        List<Comment> comments =
                commentRepository.findAllByCursor(request);

        /*
         * Repository는 Service가 hasNext를 판단할 수 있도록
         * limit + 1개를 조회한다.
         */
        assertThat(comments).hasSize(4);
    }

    @Test
    @DisplayName("특정 리뷰의 삭제되지 않은 댓글 전체 개수를 조회한다")
    void countAllComments() {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID otherReviewId = UUID.randomUUID();

        commentRepository.save(
                new Comment(
                        "댓글 1",
                        userId,
                        reviewId
                )
        );

        commentRepository.save(
                new Comment(
                        "댓글 2",
                        userId,
                        reviewId
                )
        );

        Comment deletedComment = new Comment(
                "삭제 댓글",
                userId,
                reviewId
        );

        deletedComment.softDelete();

        commentRepository.save(deletedComment);

        commentRepository.save(
                new Comment(
                        "다른 리뷰 댓글",
                        userId,
                        otherReviewId
                )
        );

        commentRepository.flush();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        50
                );

        long count =
                commentRepository.countAll(request);

        assertThat(count).isEqualTo(2L);
    }
}