package com.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.deokhugammission.comment.dto.request.CommentCreateRequest;
import com.codeit.deokhugammission.comment.dto.request.CommentUpdateRequest;
import com.codeit.deokhugammission.comment.dto.response.CommentResponse;
import com.codeit.deokhugammission.comment.entity.Comment;
import com.codeit.deokhugammission.comment.repository.CommentRepository;
import com.codeit.deokhugammission.comment.service.CommentServiceImpl;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository);
    }

    @Test
    @DisplayName("댓글을 등록할 수 있다")
    void createComment() {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "테스트 댓글입니다."
                );

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CommentResponse response =
                commentService.create(request);

        // then
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.content())
                .isEqualTo("테스트 댓글입니다.");

        verify(commentRepository)
                .save(any(Comment.class));
    }

    @Test
    @DisplayName("본인이 작성한 댓글을 수정할 수 있다")
    void updateComment() {

        // given
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

        // when
        CommentResponse response =
                commentService.update(
                        commentId,
                        userId,
                        request
                );

        // then
        assertThat(response.content())
                .isEqualTo("수정된 댓글");

        assertThat(comment.getContent())
                .isEqualTo("수정된 댓글");
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 수정할 수 없다")
    void updateCommentByOtherUser() {

        // given
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

        // when & then
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

        // given
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

        // when
        commentService.delete(
                commentId,
                userId
        );

        // then
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자는 댓글을 삭제할 수 없다")
    void deleteCommentByOtherUser() {

        // given
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

        // when & then
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

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "삭제될 댓글",
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

        // when & then
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
    @DisplayName("존재하지 않는 댓글을 수정하면 예외가 발생한다")
    void updateCommentNotFound() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정 시도"
                );

        // when & then
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
    @DisplayName("존재하지 않는 댓글을 삭제하면 예외가 발생한다")
    void deleteCommentNotFound() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());

        // when & then
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
    @DisplayName("이미 논리 삭제된 댓글을 다시 삭제해도 삭제 상태가 유지된다")
    void deleteAlreadyDeletedComment() {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        Comment comment =
                new Comment(
                        "이미 삭제된 댓글",
                        userId,
                        reviewId
                );

        comment.softDelete();

        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(comment));

        // when
        commentService.delete(
                commentId,
                userId
        );

        // then
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getDeletedAt()).isNotNull();
    }
}