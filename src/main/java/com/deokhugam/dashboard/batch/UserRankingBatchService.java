package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.UserRankingAggregationRepository.UserAggregation;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.UserRanking;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import com.deokhugam.dashboard.util.DashboardScoreCalculator;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRankingBatchService {

  private final DashboardPeriodResolver periodResolver;
  private final UserRankingAggregationRepository aggregationRepository;
  private final UserRankingRepository userRankingRepository;

  @Transactional
  public void generateAll(LocalDate baseDate) {
    for (PeriodType periodType : PeriodType.values()) {
      generatePeriod(periodType, baseDate);
    }
  }

  @Transactional
  public void generate(
      PeriodType periodType,
      LocalDate baseDate
  ) {
    generatePeriod(periodType, baseDate);
  }

  private void generatePeriod(
      PeriodType periodType,
      LocalDate baseDate
  ) {
    PeriodRange periodRange =
        periodResolver.resolve(periodType, baseDate);

    List<UserAggregation> aggregations =
        aggregationRepository.aggregate(periodRange);

    List<UserCandidate> candidates = aggregations.stream()
        .map(aggregation -> {
          double reviewScoreSum =
              DashboardScoreCalculator.calculateReviewScore(
                  aggregation.reviewLikeCount(),
                  aggregation.reviewCommentCount()
              );

          double score =
              DashboardScoreCalculator.calculatePowerUserScore(
                  reviewScoreSum,
                  aggregation.likeCount(),
                  aggregation.commentCount()
              );

          return new UserCandidate(
              aggregation,
              reviewScoreSum,
              score
          );
        })
        .sorted(
            Comparator
                .comparingDouble(UserCandidate::score)
                .reversed()
                .thenComparing(
                    candidate -> candidate.aggregation().userId()
                )
        )
        .toList();

    List<UserRanking> rankings = new ArrayList<>();

    long rank = 1L;

    for (UserCandidate candidate : candidates) {
      UserAggregation aggregation =
          candidate.aggregation();

      rankings.add(
          UserRanking.builder()
              .userId(aggregation.userId())
              .periodType(periodType)
              .ranking(rank)
              .score(candidate.score())
              .baseDate(baseDate)
              .reviewScoreSum(candidate.reviewScoreSum())
              .likeCount(aggregation.likeCount())
              .commentCount(aggregation.commentCount())
              .build()
      );

      rank++;
    }

    userRankingRepository.deleteSnapshot(
        periodType,
        baseDate
    );

    userRankingRepository.saveAll(rankings);
  }

  private record UserCandidate(
      UserAggregation aggregation,
      double reviewScoreSum,
      double score
  ) {
  }
}