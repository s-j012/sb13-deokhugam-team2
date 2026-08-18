package com.codeit.deokhugammission.comment.dto.response;

import java.util.List;

public record CommentListResponse(
        List<CommentResponse> comments
) {
}