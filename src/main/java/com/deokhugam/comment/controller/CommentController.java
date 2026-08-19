package com.deokhugam.comment.controller;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.service.CommentService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommentResponse create(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        return commentService.create(request);
    }

    @PatchMapping("/{commentId}")
    public CommentResponse update(
            @PathVariable UUID commentId,
            @RequestParam UUID userId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        return commentService.update(
                commentId,
                userId,
                request
        );
    }

    @DeleteMapping("/{commentId}")
    public void delete(
            @PathVariable UUID commentId,
            @RequestParam UUID userId
    ) {
        commentService.delete(
                commentId,
                userId
        );
    }

    @GetMapping
    public CommentListResponse findAll(
            @RequestParam UUID reviewId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime after,

            @RequestParam(defaultValue = "20")
            int limit
    ) {
        return commentService.findAll(
                reviewId,
                after,
                limit
        );
    }
}