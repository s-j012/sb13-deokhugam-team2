package com.deokhugam.dashboard.entity;

import com.deokhugam.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "user_ranking",
    indexes = {
        @Index(name = "idx_user_ranking_period_date", columnList = "period_type, base_date")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRanking extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  private PeriodType periodType;

  @Column(nullable = false)
  private long ranking;

  @Column(nullable = false)
  private double score;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(nullable = false)
  private double reviewScoreSum;

  @Column(nullable = false)
  private long likeCount;

  @Column(nullable = false)
  private long commentCount;

  @Builder
  public UserRanking(UUID userId, PeriodType periodType, long ranking, double score, LocalDate baseDate,
      double reviewScoreSum, long likeCount, long commentCount) {
    this.userId = userId;
    this.periodType = periodType;
    this.ranking = ranking;
    this.score = score;
    this.baseDate = baseDate;
    this.reviewScoreSum = reviewScoreSum;
    this.likeCount = likeCount;
    this.commentCount = commentCount;
  }
}