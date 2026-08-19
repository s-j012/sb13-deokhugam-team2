package com.deokhugam.comment.service;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentResponse create(CommentCreateRequest request) {

        Comment comment = new Comment(
                request.content(),
                request.userId(),
                request.reviewId()
        );

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    @Override
    @Transactional
    public CommentResponse update(
            UUID commentId,
            UUID userId,
            CommentUpdateRequest request
    ) {
        Comment comment = findComment(commentId);

        validateOwner(comment, userId);

        if (comment.isDeleted()) {
            throw new DeokhugamException(
                    ErrorCode.COMMENT_ALREADY_DELETED
            );
        }

        comment.updateContent(request.content());

        return CommentResponse.from(comment);
    }

    @Override
    @Transactional
    public void delete(
            UUID commentId,
            UUID userId
    ) {
        Comment comment = findComment(commentId);

        validateOwner(comment, userId);

        if (!comment.isDeleted()) {
            comment.softDelete();
        }
    }

    @Override
    public CommentListResponse findAll(
            UUID reviewId,
            LocalDateTime after,
            int limit
    ) {
        PageRequest pageable = PageRequest.of(0, limit);

        List<Comment> comments;

        if (after == null) {
            comments =
                    commentRepository
                            .findByReviewIdAndDeletedAtIsNullOrderByCreatedAtAscIdAsc(
                                    reviewId,
                                    pageable
                            );
        } else {
            comments =
                    commentRepository
                            .findByReviewIdAndDeletedAtIsNullAndCreatedAtAfterOrderByCreatedAtAscIdAsc(
                                    reviewId,
                                    after,
                                    pageable
                            );
        }

        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::from)
                .toList();

        return new CommentListResponse(responses);
    }

    private Comment findComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(
                        () -> new DeokhugamException(
                                ErrorCode.COMMENT_NOT_FOUND
                        )
                );
    }

    private void validateOwner(
            Comment comment,
            UUID userId
    ) {
        if (!comment.getUserId().equals(userId)) {
            throw new DeokhugamException(
                    ErrorCode.COMMENT_ACCESS_DENIED
            );
        }
    }
}