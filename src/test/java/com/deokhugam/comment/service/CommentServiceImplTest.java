package com.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService =
                new CommentServiceImpl(commentRepository);
    }

    @Test
    @DisplayName("댓글을 등록할 수 있다")
    void createComment() {

        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "테스트 댓글입니다."
                );

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );

        CommentResponse response =
                commentService.create(request);

        assertThat(response.userId())
                .isEqualTo(userId);

        assertThat(response.reviewId())
                .isEqualTo(reviewId);

        assertThat(response.content())
                .isEqualTo("테스트 댓글입니다.");

        verify(commentRepository)
                .save(any(Comment.class));
    }

    @Test
    @DisplayName("본인이 작성한 댓글을 수정할 수 있다")
    void updateComment() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "수정 전 댓글",
                        userId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정된 댓글"
                );

        CommentResponse response =
                commentService.update(
                        commentId,
                        userId,
                        request
                );

        assertThat(response.content())
                .isEqualTo("수정된 댓글");

        assertThat(comment.getContent())
                .isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 수정할 수 없다")
    void updateCommentByOtherUser() {

        UUID commentId = UUID.randomUUID();

        UUID writerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "댓글 내용",
                        writerId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        otherUserId,
                        request
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_ACCESS_DENIED
                    );
                });
    }

    @Test
    @DisplayName("댓글을 논리 삭제할 수 있다")
    void softDeleteComment() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "삭제할 댓글",
                        userId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        commentService.delete(
                commentId,
                userId
        );

        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 삭제할 수 없다")
    void deleteCommentByOtherUser() {

        UUID commentId = UUID.randomUUID();

        UUID writerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "댓글 내용",
                        writerId,
                        reviewId
                );

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        assertThatThrownBy(
                () -> commentService.delete(
                        commentId,
                        otherUserId
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_ACCESS_DENIED
                    );
                });

        assertThat(comment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제된 댓글은 수정할 수 없다")
    void updateDeletedComment() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "삭제된 댓글",
                        userId,
                        reviewId
                );

        comment.softDelete();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        userId,
                        request
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_ALREADY_DELETED
                    );
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정 시 예외가 발생한다")
    void updateCommentNotFound() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        assertThatThrownBy(
                () -> commentService.update(
                        commentId,
                        userId,
                        request
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_NOT_FOUND
                    );
                });
    }

    @Test
    @DisplayName("존재하지 않는 댓글 삭제 시 예외가 발생한다")
    void deleteCommentNotFound() {

        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> commentService.delete(
                        commentId,
                        userId
                )
        )
                .isInstanceOf(DeokhugamException.class)
                .satisfies(exception -> {

                    DeokhugamException deokhugamException =
                            (DeokhugamException) exception;

                    assertThat(
                            deokhugamException.getErrorCode()
                    ).isEqualTo(
                            ErrorCode.COMMENT_NOT_FOUND
                    );
                });
    }

    @Test
    @DisplayName("다음 페이지가 있으면 limit만큼 반환하고 nextCursor를 생성한다")
    void findAllWithNextPage() {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        2
                );

        Comment first = new Comment(
                "첫 번째",
                userId,
                reviewId
        );

        Comment second = new Comment(
                "두 번째",
                userId,
                reviewId
        );

        Comment third = new Comment(
                "세 번째",
                userId,
                reviewId
        );

        LocalDateTime firstCreatedAt =
                LocalDateTime.of(2026, 8, 23, 12, 0);

        LocalDateTime secondCreatedAt =
                LocalDateTime.of(2026, 8, 23, 11, 0);

        LocalDateTime thirdCreatedAt =
                LocalDateTime.of(2026, 8, 23, 10, 0);

        /*
         * Mockito 단위 테스트에서는 JPA Auditing이 동작하지 않으므로
         * BaseEntity의 createdAt을 테스트에서 직접 설정한다.
         */
        ReflectionTestUtils.setField(
                first,
                "createdAt",
                firstCreatedAt
        );

        ReflectionTestUtils.setField(
                second,
                "createdAt",
                secondCreatedAt
        );

        ReflectionTestUtils.setField(
                third,
                "createdAt",
                thirdCreatedAt
        );

        when(commentRepository.findAllByCursor(request))
                .thenReturn(
                        List.of(
                                first,
                                second,
                                third
                        )
                );

        when(commentRepository.countAll(request))
                .thenReturn(3L);

        // when
        CommentListResponse response =
                commentService.findAll(request);

        // then
        assertThat(response.content())
                .hasSize(2);

        assertThat(response.content().get(0).content())
                .isEqualTo("첫 번째");

        assertThat(response.content().get(1).content())
                .isEqualTo("두 번째");

        assertThat(response.size())
                .isEqualTo(2);

        assertThat(response.totalElements())
                .isEqualTo(3L);

        assertThat(response.hasNext())
                .isTrue();

        assertThat(response.nextCursor())
                .isEqualTo(secondCreatedAt.toString());

        assertThat(response.nextAfter())
                .isEqualTo(secondCreatedAt);
    }

    @Test
    @DisplayName("다음 페이지가 없으면 전체 댓글과 totalElements를 반환한다")
    void findAllWithoutNextPage() {

        UUID reviewId = UUID.randomUUID();

        CommentSearchRequest request =
                new CommentSearchRequest(
                        reviewId,
                        "DESC",
                        null,
                        null,
                        10
                );

        when(commentRepository.findAllByCursor(request))
                .thenReturn(List.of());

        when(commentRepository.countAll(request))
                .thenReturn(0L);

        CommentListResponse response =
                commentService.findAll(request);

        assertThat(response.content()).isEmpty();
        assertThat(response.size()).isZero();
        assertThat(response.totalElements()).isZero();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
        assertThat(response.nextAfter()).isNull();
    }
}