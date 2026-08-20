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
    name = "book_ranking",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_book_ranking_period_date_book",
            columnNames = {"period_type", "base_date", "book_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_book_ranking_period_date_rank",
            columnList = "period_type, base_date, ranking"
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookRanking extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "book_id", nullable = false)
  private UUID bookId;

  @Enumerated(EnumType.STRING)
  @Column(name = "period_type", nullable = false)
  private PeriodType periodType;

  @Column(nullable = false)
  private long ranking;

  @Column(nullable = false)
  private double score;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "review_count", nullable = false)
  private long reviewCount;

  @Column(nullable = false)
  private double rating;

  @Builder
  public BookRanking(
      UUID bookId,
      PeriodType periodType,
      long ranking,
      double score,
      LocalDate baseDate,
      long reviewCount,
      double rating
  ) {
    this.bookId = bookId;
    this.periodType = periodType;
    this.ranking = ranking;
    this.score = score;
    this.baseDate = baseDate;
    this.reviewCount = reviewCount;
    this.rating = rating;
  }
}
