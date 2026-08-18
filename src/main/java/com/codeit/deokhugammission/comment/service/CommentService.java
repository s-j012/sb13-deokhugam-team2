package com.codeit.deokhugammission.comment.service;

import com.codeit.deokhugammission.comment.dto.request.CommentCreateRequest;
import com.codeit.deokhugammission.comment.dto.request.CommentUpdateRequest;
import com.codeit.deokhugammission.comment.dto.response.CommentListResponse;
import com.codeit.deokhugammission.comment.dto.response.CommentResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public interface CommentService {

    CommentResponse create(CommentCreateRequest request);

    CommentResponse update(
            UUID commentId,
            UUID userId,
            CommentUpdateRequest request
    );

    void delete(
            UUID commentId,
            UUID userId
    );

    CommentListResponse findAll(
            UUID reviewId,
            LocalDateTime after,
            int limit
    );
}