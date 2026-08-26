package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.UserRankingAggregationRepository.UserAggregation;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.UserRanking;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRankingBatchServiceTest {

  @Mock
  private DashboardPeriodResolver periodResolver;

  @Mock
  private UserRankingAggregationRepository aggregationRepository;

  @Mock
  private UserRankingRepository userRankingRepository;

  @InjectMocks
  private UserRankingBatchService userRankingBatchService;

  @Captor
  private ArgumentCaptor<List<UserRanking>> rankingsCaptor;

  @Test
  @DisplayName("파워 유저 점수 순으로 랭킹을 생성한다")
  void generateUserRanking() {
    LocalDate baseDate = LocalDate.of(2026, 8, 26);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 26, 0, 0),
        LocalDateTime.of(2026, 8, 27, 0, 0)
    );

    UUID firstUserId =
        UUID.fromString("00000000-0000-0000-0000-000000000001");

    UUID secondUserId =
        UUID.fromString("00000000-0000-0000-0000-000000000002");

    UUID thirdUserId =
        UUID.fromString("00000000-0000-0000-0000-000000000003");

    when(periodResolver.resolve(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of(
            new UserAggregation(
                secondUserId,
                20L,
                5L,
                1L,
                0L
            ),
            new UserAggregation(
                thirdUserId,
                0L,
                0L,
                10L,
                1L
            ),
            new UserAggregation(
                firstUserId,
                10L,
                10L,
                0L,
                0L
            )
        ));

    userRankingBatchService.generate(
        PeriodType.DAILY,
        baseDate
    );

    InOrder inOrder = inOrder(userRankingRepository);

    inOrder.verify(userRankingRepository)
        .deleteSnapshot(
            PeriodType.DAILY,
            baseDate
        );

    inOrder.verify(userRankingRepository)
        .saveAll(rankingsCaptor.capture());

    List<UserRanking> rankings =
        rankingsCaptor.getValue();

    assertThat(rankings).hasSize(3);

    assertThat(rankings)
        .extracting(UserRanking::getUserId)
        .containsExactly(
            firstUserId,
            secondUserId,
            thirdUserId
        );

    assertThat(rankings)
        .extracting(UserRanking::getRanking)
        .containsExactly(
            1L,
            2L,
            3L
        );

    assertThat(rankings)
        .extracting(UserRanking::getReviewScoreSum)
        .containsExactly(
            10.0,
            9.5,
            0.0
        );

    assertThat(rankings)
        .extracting(UserRanking::getScore)
        .containsExactly(
            5.0,
            4.95,
            2.3
        );

    assertThat(rankings.get(0).getPeriodType())
        .isEqualTo(PeriodType.DAILY);

    assertThat(rankings.get(0).getBaseDate())
        .isEqualTo(baseDate);

    assertThat(rankings.get(1).getLikeCount())
        .isEqualTo(1L);

    assertThat(rankings.get(2).getLikeCount())
        .isEqualTo(10L);

    assertThat(rankings.get(2).getCommentCount())
        .isEqualTo(1L);
  }

  @Test
  @DisplayName("집계 결과가 없어도 기존 파워 유저 랭킹 스냅샷을 삭제한다")
  void clearSnapshotWhenNoRankingExists() {
    LocalDate baseDate = LocalDate.of(2026, 8, 26);

    PeriodRange periodRange = new PeriodRange(
        LocalDateTime.of(2026, 8, 26, 0, 0),
        LocalDateTime.of(2026, 8, 27, 0, 0)
    );

    when(periodResolver.resolve(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(periodRange);

    when(aggregationRepository.aggregate(periodRange))
        .thenReturn(List.of());

    userRankingBatchService.generate(
        PeriodType.DAILY,
        baseDate
    );

    verify(userRankingRepository)
        .deleteSnapshot(
            PeriodType.DAILY,
            baseDate
        );

    verify(userRankingRepository)
        .saveAll(List.of());
  }
}