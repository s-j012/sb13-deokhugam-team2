package com.deokhugam.dashboard.dto.response;

import com.deokhugam.dashboard.entity.PeriodType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PowerUserDto(
    UUID userId,             // 유저 식별자 (PK)
    String nickname,         // 유저 닉네임
    PeriodType period,       // 랭킹 산출 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)
    long rank,                // 순위
    double score,            // 대시보드 종합 점수 (리뷰점수 합 * 0.5 + 좋아요 * 0.2 + 댓글 * 0.3)
    double reviewScoreSum,   // 유저가 작성한 리뷰들의 인기점수 총합
    long likeCount,           // 유저가 받은 좋아요 총 개수
    long commentCount,        // 유저가 받은 댓글 총 개수
    LocalDateTime createdAt  // 랭킹 데이터 생성 일시
) {
}