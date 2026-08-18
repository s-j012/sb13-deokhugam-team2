package com.deokhugam.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.deokhugammission.comment.controller.CommentController;
import com.codeit.deokhugammission.comment.dto.request.CommentCreateRequest;
import com.codeit.deokhugammission.comment.dto.request.CommentUpdateRequest;
import com.codeit.deokhugammission.comment.dto.response.CommentListResponse;
import com.codeit.deokhugammission.comment.dto.response.CommentResponse;
import com.codeit.deokhugammission.comment.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CommentService commentService;

    @BeforeEach
    void setUp() {

        CommentController commentController =
                new CommentController(commentService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("댓글을 등록할 수 있다")
    void createComment() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        "테스트 댓글입니다."
                );

        CommentResponse response =
                new CommentResponse(
                        commentId,
                        userId,
                        reviewId,
                        "테스트 댓글입니다.",
                        LocalDateTime.of(2026, 8, 18, 10, 0),
                        LocalDateTime.of(2026, 8, 18, 10, 0)
                );

        when(commentService.create(any(CommentCreateRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(commentId.toString())
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(userId.toString())
                )
                .andExpect(
                        jsonPath("$.reviewId")
                                .value(reviewId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("테스트 댓글입니다.")
                );
    }

    @Test
    @DisplayName("댓글 내용을 수정할 수 있다")
    void updateComment() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentUpdateRequest request =
                new CommentUpdateRequest(
                        "수정된 댓글"
                );

        CommentResponse response =
                new CommentResponse(
                        commentId,
                        userId,
                        reviewId,
                        "수정된 댓글",
                        LocalDateTime.of(2026, 8, 18, 10, 0),
                        LocalDateTime.of(2026, 8, 18, 11, 0)
                );

        when(
                commentService.update(
                        eq(commentId),
                        eq(userId),
                        any(CommentUpdateRequest.class)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/comments/{commentId}",
                                commentId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(commentId.toString())
                )
                .andExpect(
                        jsonPath("$.content")
                                .value("수정된 댓글")
                );
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다")
    void deleteComment() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        doNothing()
                .when(commentService)
                .delete(commentId, userId);

        // when & then
        mockMvc.perform(
                        delete(
                                "/api/comments/{commentId}",
                                commentId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("리뷰별 댓글 목록을 조회할 수 있다")
    void findAllComments() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentResponse first =
                new CommentResponse(
                        UUID.randomUUID(),
                        userId,
                        reviewId,
                        "첫 번째 댓글",
                        LocalDateTime.of(2026, 8, 18, 10, 0),
                        LocalDateTime.of(2026, 8, 18, 10, 0)
                );

        CommentResponse second =
                new CommentResponse(
                        UUID.randomUUID(),
                        userId,
                        reviewId,
                        "두 번째 댓글",
                        LocalDateTime.of(2026, 8, 18, 11, 0),
                        LocalDateTime.of(2026, 8, 18, 11, 0)
                );

        CommentListResponse response =
                new CommentListResponse(
                        List.of(first, second)
                );

        when(
                commentService.findAll(
                        eq(reviewId),
                        eq(null),
                        eq(20)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.comments.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.comments[0].content")
                                .value("첫 번째 댓글")
                )
                .andExpect(
                        jsonPath("$.comments[1].content")
                                .value("두 번째 댓글")
                );
    }

    @Test
    @DisplayName("after와 limit으로 댓글 목록을 조회할 수 있다")
    void findAllCommentsWithAfterAndLimit() throws Exception {

        // given
        UUID reviewId = UUID.randomUUID();

        LocalDateTime after =
                LocalDateTime.of(
                        2026,
                        8,
                        18,
                        10,
                        0
                );

        CommentListResponse response =
                new CommentListResponse(
                        List.of()
                );

        when(
                commentService.findAll(
                        eq(reviewId),
                        eq(after),
                        eq(10)
                )
        ).thenReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/comments")
                                .param(
                                        "reviewId",
                                        reviewId.toString()
                                )
                                .param(
                                        "after",
                                        "2026-08-18T10:00:00"
                                )
                                .param(
                                        "limit",
                                        "10"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.comments.length()")
                                .value(0)
                );
    }

    @Test
    @DisplayName("댓글 등록 시 내용이 비어 있으면 400을 반환한다")
    void createCommentWithBlankContent() throws Exception {

        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(
                        userId,
                        reviewId,
                        ""
                );

        // when & then
        mockMvc.perform(
                        post("/api/comments")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 수정 시 내용이 비어 있으면 400을 반환한다")
    void updateCommentWithBlankContent() throws Exception {

        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentUpdateRequest request =
                new CommentUpdateRequest("");

        // when & then
        mockMvc.perform(
                        patch(
                                "/api/comments/{commentId}",
                                commentId
                        )
                                .param(
                                        "userId",
                                        userId.toString()
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest());
    }
}