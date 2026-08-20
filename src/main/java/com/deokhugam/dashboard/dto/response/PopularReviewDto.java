package com.deokhugam.dashboard.dto.response;

import com.deokhugam.dashboard.entity.PeriodType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PopularReviewDto(
    UUID id,                 // 대시보드 랭킹 테이블의 PK
    UUID reviewId,           // 리뷰 식별자 (PK)
    UUID bookId,             // 리뷰가 작성된 도서의 식별자 (PK)
    String bookTitle,        // 도서 제목
    String bookThumbnailUrl, // 도서 썸네일 이미지 URL
    UUID userId,             // 리뷰 작성자 식별자 (PK)
    String userNickname,     // 리뷰 작성자 닉네임
    String reviewContent,    // 리뷰 본문 내용
    double reviewRating,     // 리뷰어가 남긴 평점
    PeriodType period,       // 랭킹 산출 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)
    long rank,                // 순위
    double score,            // 대시보드 점수 (좋아요 수 * 0.3 + 댓글 수 * 0.7)
    long likeCount,           // 리뷰가 받은 좋아요 총 개수
    long commentCount,        // 리뷰에 달린 댓글 총 개수
    LocalDateTime createdAt  // 랭킹 데이터 생성 일시
) {
}