package com.deokhugam.dashboard.entity;

import com.deokhugam.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_ranking",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_ranking_period_date_user",
            columnNames = {"period_type", "base_date", "user_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_user_ranking_period_date_rank",
            columnList = "period_type, base_date, ranking"
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRanking extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
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

  @Column(name = "review_score_sum", nullable = false)
  private double reviewScoreSum;

  @Column(name = "like_count", nullable = false)
  private long likeCount;

  @Column(name = "comment_count", nullable = false)
  private long commentCount;

  @Builder
  public UserRanking(
      UUID userId,
      PeriodType periodType,
      long ranking,
      double score,
      LocalDate baseDate,
      double reviewScoreSum,
      long likeCount,
      long commentCount
  ) {
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
