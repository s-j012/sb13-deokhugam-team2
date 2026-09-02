package com.deokhugam.dashboard.service;

import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.dashboard.dto.response.PopularBookDto;
import com.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.deokhugam.dashboard.dto.response.PowerUserDto;
import com.deokhugam.dashboard.entity.BookRanking;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.entity.ReviewRanking;
import com.deokhugam.dashboard.entity.UserRanking;
import com.deokhugam.dashboard.exception.InvalidDashboardPaginationException;
import com.deokhugam.dashboard.repository.BookRankingRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import com.deokhugam.global.storage.Storage;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.repository.UserRepository;
import com.deokhugam.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
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

  @Mock
  private Storage storage;

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

    verify(bookRepository)
        .findAllByIdInAndDeletedAtIsNull(List.of());
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

    verify(reviewRepository)
        .findAllByIdInAndDeletedAtIsNull(List.of());

    verify(bookRepository)
        .findAllByIdInAndDeletedAtIsNull(List.of());

    verify(userRepository)
        .findAllByIdInAndDeletedAtIsNull(List.of());
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

    verify(userRepository)
        .findAllByIdInAndDeletedAtIsNull(List.of());
  }

  @Test
  @DisplayName("인기 도서의 썸네일 저장 경로를 접근 URL로 변환한다")
  void convertThumbnailUrl() {
    LocalDate baseDate = LocalDate.of(2026, 8, 20);
    UUID bookId = UUID.randomUUID();

    BookRanking ranking = mock(BookRanking.class);
    Book book = mock(Book.class);

    when(ranking.getBookId()).thenReturn(bookId);
    when(book.getId()).thenReturn(bookId);
    when(book.getThumbnailUrl()).thenReturn("books/test.jpg");

    when(bookRankingRepository.findLatestBaseDate(PeriodType.DAILY))
        .thenReturn(Optional.of(baseDate));

    when(bookRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(1L);

    when(bookRankingRepository.findRankingPageAsc(
        eq(PeriodType.DAILY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    )).thenReturn(List.of(ranking));

    when(bookRepository.findAllByIdInAndDeletedAtIsNull(
        List.of(bookId)
    )).thenReturn(List.of(book));

    when(storage.getUrl("books/test.jpg"))
        .thenReturn("https://example.com/books/test.jpg");

    dashboardQueryService.getPopularBooks(
        PeriodType.DAILY,
        Sort.Direction.ASC,
        null,
        null,
        50
    );

    verify(storage).getUrl("books/test.jpg");
  }

  @Test
  @DisplayName("대시보드 조회 요청 값을 검증한다")
  void validateRequest() {
    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            null,
            Sort.Direction.ASC,
            null,
            null,
            50
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);

    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            null,
            null,
            null,
            50
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);

    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            0
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);

    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            Integer.MAX_VALUE
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);

    assertThatThrownBy(() ->
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            "-1",
            LocalDateTime.now(),
            50
        )
    ).isInstanceOf(InvalidDashboardPaginationException.class);
  }

  @Test
  @DisplayName("인기 리뷰 조회 결과를 DTO로 변환한다")
  void getPopularReviewsWithData() {
    LocalDate baseDate = LocalDate.of(2026, 8, 27);
    LocalDateTime createdAt =
        LocalDateTime.of(2026, 8, 27, 1, 0);

    UUID reviewId = UUID.randomUUID();
    UUID bookId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    ReviewRanking ranking = mock(ReviewRanking.class);
    Review review = mock(Review.class);
    Book book = mock(Book.class);
    User user = mock(User.class);

    when(ranking.getReviewId()).thenReturn(reviewId);
    when(ranking.getPeriodType()).thenReturn(PeriodType.DAILY);
    when(ranking.getRanking()).thenReturn(1L);
    when(ranking.getScore()).thenReturn(8.5);
    when(ranking.getLikeCount()).thenReturn(5L);
    when(ranking.getCommentCount()).thenReturn(10L);
    when(ranking.getCreatedAt()).thenReturn(createdAt);

    when(review.getId()).thenReturn(reviewId);
    when(review.getBook()).thenReturn(book);
    when(review.getUser()).thenReturn(user);
    when(review.getContent()).thenReturn("테스트 리뷰");
    when(review.getRating()).thenReturn(5);

    when(book.getId()).thenReturn(bookId);
    when(book.getTitle()).thenReturn("테스트 도서");

    when(user.getId()).thenReturn(userId);
    when(user.getNickname()).thenReturn("테스트유저");

    when(reviewRankingRepository.findLatestBaseDate(
        PeriodType.DAILY
    )).thenReturn(Optional.of(baseDate));

    when(reviewRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(1L);

    when(reviewRankingRepository.findRankingPageAsc(
        eq(PeriodType.DAILY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    )).thenReturn(List.of(ranking));

    when(reviewRepository.findAllByIdInAndDeletedAtIsNull(
        List.of(reviewId)
    )).thenReturn(List.of(review));

    when(bookRepository.findAllByIdInAndDeletedAtIsNull(
        List.of(bookId)
    )).thenReturn(List.of(book));

    when(userRepository.findAllByIdInAndDeletedAtIsNull(
        List.of(userId)
    )).thenReturn(List.of(user));

    CursorPageResponse<PopularReviewDto> response =
        dashboardQueryService.getPopularReviews(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            50
        );

    assertThat(response.content()).hasSize(1);

    PopularReviewDto result =
        response.content().get(0);

    assertThat(result.reviewId())
        .isEqualTo(reviewId);

    assertThat(result.bookId())
        .isEqualTo(bookId);

    assertThat(result.bookTitle())
        .isEqualTo("테스트 도서");

    assertThat(result.userId())
        .isEqualTo(userId);

    assertThat(result.userNickname())
        .isEqualTo("테스트유저");

    assertThat(result.reviewContent())
        .isEqualTo("테스트 리뷰");

    assertThat(result.reviewRating())
        .isEqualTo(5.0);
  }

  @Test
  @DisplayName("파워 유저 조회 결과를 DTO로 변환한다")
  void getPowerUsersWithData() {
    LocalDate baseDate = LocalDate.of(2026, 8, 27);
    LocalDateTime createdAt =
        LocalDateTime.of(2026, 8, 27, 1, 0);

    UUID userId = UUID.randomUUID();

    UserRanking ranking = mock(UserRanking.class);
    User user = mock(User.class);

    when(ranking.getUserId()).thenReturn(userId);
    when(ranking.getPeriodType()).thenReturn(PeriodType.DAILY);
    when(ranking.getRanking()).thenReturn(1L);
    when(ranking.getScore()).thenReturn(10.0);
    when(ranking.getReviewScoreSum()).thenReturn(15.0);
    when(ranking.getLikeCount()).thenReturn(5L);
    when(ranking.getCommentCount()).thenReturn(5L);
    when(ranking.getCreatedAt()).thenReturn(createdAt);

    when(user.getId()).thenReturn(userId);
    when(user.getNickname()).thenReturn("파워유저");

    when(userRankingRepository.findLatestBaseDate(
        PeriodType.DAILY
    )).thenReturn(Optional.of(baseDate));

    when(userRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(1L);

    when(userRankingRepository.findRankingPageAsc(
        eq(PeriodType.DAILY),
        eq(baseDate),
        isNull(),
        isNull(),
        any(Pageable.class)
    )).thenReturn(List.of(ranking));

    when(userRepository.findAllByIdInAndDeletedAtIsNull(
        List.of(userId)
    )).thenReturn(List.of(user));

    CursorPageResponse<PowerUserDto> response =
        dashboardQueryService.getPowerUsers(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            null,
            null,
            50
        );

    assertThat(response.content()).hasSize(1);

    PowerUserDto result =
        response.content().get(0);

    assertThat(result.userId())
        .isEqualTo(userId);

    assertThat(result.nickname())
        .isEqualTo("파워유저");

    assertThat(result.reviewScoreSum())
        .isEqualTo(15.0);

    assertThat(result.likeCount())
        .isEqualTo(5L);

    assertThat(result.commentCount())
        .isEqualTo(5L);
  }

  @Test
  @DisplayName("다음 페이지가 있으면 커서와 after를 반환한다")
  void createNextCursor() {
    LocalDate baseDate = LocalDate.of(2026, 8, 27);
    LocalDateTime after =
        LocalDateTime.of(2026, 8, 27, 0, 30);
    LocalDateTime createdAt =
        LocalDateTime.of(2026, 8, 27, 1, 0);

    UUID bookId = UUID.randomUUID();

    BookRanking firstRanking = mock(BookRanking.class);
    BookRanking extraRanking = mock(BookRanking.class);
    Book book = mock(Book.class);

    when(firstRanking.getBookId()).thenReturn(bookId);
    when(firstRanking.getRanking()).thenReturn(2L);
    when(firstRanking.getCreatedAt()).thenReturn(createdAt);

    when(book.getId()).thenReturn(bookId);

    when(bookRankingRepository.findLatestBaseDate(
        PeriodType.DAILY
    )).thenReturn(Optional.of(baseDate));

    when(bookRankingRepository.countByPeriodTypeAndBaseDate(
        PeriodType.DAILY,
        baseDate
    )).thenReturn(2L);

    when(bookRankingRepository.findRankingPageAsc(
        eq(PeriodType.DAILY),
        eq(baseDate),
        eq(1L),
        eq(after),
        any(Pageable.class)
    )).thenReturn(List.of(
        firstRanking,
        extraRanking
    ));

    when(bookRepository.findAllByIdInAndDeletedAtIsNull(
        List.of(bookId)
    )).thenReturn(List.of(book));

    CursorPageResponse<PopularBookDto> response =
        dashboardQueryService.getPopularBooks(
            PeriodType.DAILY,
            Sort.Direction.ASC,
            "1",
            after,
            1
        );

    assertThat(response.hasNext())
        .isTrue();

    assertThat(response.nextCursor())
        .isEqualTo("2");

    assertThat(response.nextAfter())
        .isEqualTo(createdAt);

    assertThat(response.size())
        .isEqualTo(1);

    assertThat(response.totalElements())
        .isEqualTo(2L);
  }
}