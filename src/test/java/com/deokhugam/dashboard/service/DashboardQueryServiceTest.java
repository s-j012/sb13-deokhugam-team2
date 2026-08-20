package com.deokhugam.dashboard.service;

import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.exception.InvalidDashboardPaginationException;
import com.deokhugam.dashboard.repository.BookRankingRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardQueryServiceTest {

  @Mock
  private BookRankingRepository bookRankingRepository;

  @Mock
  private ReviewRankingRepository reviewRankingRepository;

  @Mock
  private UserRankingRepository userRankingRepository;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private DashboardQueryService dashboardQueryService;


  @Test
  @DisplayName("cursor와 after는 함께 전달해야 한다")
  void validateCursorPair() {
    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            "10",
            null,
            50
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);

    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            LocalDateTime.now(),
            50
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);
  }


  @Test
  @DisplayName("cursor는 숫자 형식이어야 한다")
  void validateCursorFormat() {
    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            "invalid",
            LocalDateTime.now(),
            50
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);
  }


  @Test
  @DisplayName("랭킹 데이터가 없으면 페이지 조회를 수행하지 않는다")
  void emptyRankingData() {
    when(bookRankingRepository.findLatestBaseDate(PeriodType.DAILY))
        .thenReturn(Optional.empty());

    dashboardQueryService.getPopularBooks(
        PeriodType.DAILY,
        Sort.Direction.ASC,
        null,
        null,
        50
    );

    verify(bookRankingRepository, never()).findRankingPageAsc(
        any(),
        any(),
        any(),
        any(),
        any()
    );

    verify(bookRankingRepository, never()).findRankingPageDesc(
        any(),
        any(),
        any(),
        any(),
        any()
    );
  }


  @Test
  @DisplayName("인기 도서를 오름차순으로 조회한다")
  void getPopularBooks() {
    LocalDate baseDate = LocalDate.of(2026, 8, 20);

    when(bookRankingRepository.findLatestBaseDate(PeriodType.DAILY))
        .thenReturn(Optional.of(baseDate));

    when(bookRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(0L);

    when(bookRankingRepository.findRankingPageAsc(
        eq(PeriodType.DAILY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    )).thenReturn(List.of());

    dashboardQueryService.getPopularBooks(
        PeriodType.DAILY,
        Sort.Direction.ASC,
        null,
        null,
        50
    );

    verify(bookRankingRepository).findRankingPageAsc(
        eq(PeriodType.DAILY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    );
  }


  @Test
  @DisplayName("인기 리뷰를 내림차순으로 조회한다")
  void getPopularReviews() {
    LocalDate baseDate = LocalDate.of(2026, 8, 20);

    when(reviewRankingRepository.findLatestBaseDate(PeriodType.WEEKLY))
        .thenReturn(Optional.of(baseDate));

    when(reviewRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.WEEKLY,
        baseDate
    )).thenReturn(0L);

    when(reviewRankingRepository.findRankingPageDesc(
        eq(PeriodType.WEEKLY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    )).thenReturn(List.of());

    dashboardQueryService.getPopularReviews(
        PeriodType.WEEKLY,
        Sort.Direction.DESC,
        null,
        null,
        50
    );

    verify(reviewRankingRepository).findRankingPageDesc(
        eq(PeriodType.WEEKLY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    );
  }


  @Test
  @DisplayName("파워 유저를 오름차순으로 조회한다")
  void getPowerUsers() {
    LocalDate baseDate = LocalDate.of(2026, 8, 20);

    when(userRankingRepository.findLatestBaseDate(PeriodType.MONTHLY))
        .thenReturn(Optional.of(baseDate));

    when(userRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.MONTHLY,
        baseDate
    )).thenReturn(0L);

    when(userRankingRepository.findRankingPageAsc(
        eq(PeriodType.MONTHLY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    )).thenReturn(List.of());

    dashboardQueryService.getPowerUsers(
        PeriodType.MONTHLY,
        Sort.Direction.ASC,
        null,
        null,
        50
    );

    verify(userRankingRepository).findRankingPageAsc(
        eq(PeriodType.MONTHLY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    );
  }
}