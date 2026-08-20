package com.deokhugam.dashboard.dto.response;

import com.deokhugam.dashboard.entity.PeriodType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PopularBookDto(
    UUID id,                 // 대시보드 랭킹 테이블의 PK
    UUID bookId,             // 도서 식별자 (PK)
    String title,            // 도서 제목
    String author,           // 저자명
    String thumbnailUrl,     // 도서 썸네일 이미지 URL
    PeriodType period,       // 랭킹 산출 기간 (DAILY, WEEKLY, MONTHLY, ALL_TIME)
    long rank,               // 순위
    double score,            // 대시보드 점수 (리뷰 수 * 0.4 + 평점 평균 * 0.6)
    long reviewCount,        // 도서에 작성된 리뷰 총 개수
    double rating,           // 도서의 평균 평점
    LocalDateTime createdAt  // 랭킹 데이터 생성 일시
) {
}