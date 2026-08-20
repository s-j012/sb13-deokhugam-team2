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
import com.deokhugam.dashboard.repository.BookRankingRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService {

  private final BookRankingRepository bookRankingRepository;
  private final ReviewRankingRepository reviewRankingRepository;
  private final UserRankingRepository userRankingRepository;

  private final BookRepository bookRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  private <T> CursorPageResponse<T> emptyPage() {
    return new CursorPageResponse<>(List.of(), null, null, 0, 0L, false);
  }

  private long parseRankingCursor(String cursor, Sort.Direction direction) {
    if (cursor == null || cursor.isBlank()) {
      return direction.isAscending() ? 0L : Long.MAX_VALUE;
    }
    try {
      long ranking = Long.parseLong(cursor);
      if (ranking < 0) {
        throw new IllegalArgumentException("cursor는 0 이상이어야 합니다.");
      }
      return ranking;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("cursor는 숫자(순위) 형식이어야 합니다.", e);
    }
  }

  private <T> CursorPageResponse<T> createCursorPageResponse(
      List<T> content, boolean hasNext, long totalElements,
      Function<T, Long> rankExtractor, Function<T, LocalDateTime> dateExtractor) {

    String nextCursor = (hasNext && !content.isEmpty()) ? String.valueOf(rankExtractor.apply(content.get(content.size() - 1))) : null;
    LocalDateTime nextAfter = (hasNext && !content.isEmpty()) ? dateExtractor.apply(content.get(content.size() - 1)) : null;

    return new CursorPageResponse<>(content, nextCursor, nextAfter, content.size(), totalElements, hasNext);
  }

  private <T> PageResult<T> fetchRankingPageData(
      PeriodType period, Sort.Direction direction, String cursor, int limit,
      Function<PeriodType, Optional<LocalDate>> baseDateFetcher,
      BiFunction<PeriodType, LocalDate, Long> countFetcher,
      RankingQueryFunction<T> ascQuery,
      RankingQueryFunction<T> descQuery) {

    LocalDate baseDate = baseDateFetcher.apply(period).orElse(null);
    if (baseDate == null) {
      return new PageResult<>(List.of(), 0, false, true); // 데이터가 비어있음을(true) 반환
    }

    long totalElements = countFetcher.apply(period, baseDate);
    long cursorRanking = parseRankingCursor(cursor, direction);
    PageRequest pageRequest = PageRequest.of(0, limit + 1);

    List<T> fetched = direction.isAscending()
        ? ascQuery.execute(period, baseDate, cursorRanking, pageRequest)
        : descQuery.execute(period, baseDate, cursorRanking, pageRequest);

    boolean hasNext = fetched.size() > limit;
    List<T> pageItems = hasNext ? fetched.subList(0, limit) : fetched;

    return new PageResult<>(pageItems, totalElements, hasNext, false);
  }

  private record PageResult<T>(List<T> items, long totalElements, boolean hasNext, boolean isEmpty) {}

  @FunctionalInterface
  private interface RankingQueryFunction<T> {
    List<T> execute(PeriodType period, LocalDate baseDate, long ranking, PageRequest pageRequest);
  }

  // 인기 도서 목록 조회
  public CursorPageResponse<PopularBookDto> getPopularBooks(PeriodType period, Sort.Direction direction, String cursor, LocalDateTime after, int limit) {
    PageResult<BookRanking> pageData = fetchRankingPageData(
        period, direction, cursor, limit,
        bookRankingRepository::findLatestBaseDate,
        bookRankingRepository::countByPeriodTypeAndBaseDate,
        bookRankingRepository::findByPeriodTypeAndBaseDateAndRankingGreaterThanOrderByRankingAsc,
        bookRankingRepository::findByPeriodTypeAndBaseDateAndRankingLessThanOrderByRankingDesc
    );

    if (pageData.isEmpty()) return emptyPage();

    List<UUID> bookIds = pageData.items().stream().map(BookRanking::getBookId).collect(Collectors.toList());
    Map<UUID, Book> bookMap = bookRepository.findAllById(bookIds).stream().collect(Collectors.toMap(Book::getId, b -> b));

    List<PopularBookDto> content = pageData.items().stream().map(ranking -> {
      Book book = bookMap.get(ranking.getBookId());
      return PopularBookDto.builder()
          .id(ranking.getId())
          .bookId(ranking.getBookId())
          .title(book != null ? book.getTitle() : "삭제된 도서")
          .author(book != null ? book.getAuthor() : "알 수 없음")
          .thumbnailUrl(book != null ? book.getThumbnailUrl() : null)
          .period(ranking.getPeriodType())
          .rank(ranking.getRanking())
          .score(ranking.getScore())
          .reviewCount(ranking.getReviewCount())
          .rating(ranking.getRating())
          .createdAt(ranking.getCreatedAt())
          .build();
    }).collect(Collectors.toList());

    return createCursorPageResponse(content, pageData.hasNext(), pageData.totalElements(), PopularBookDto::rank, PopularBookDto::createdAt);
  }

  // 인기 리뷰 목록 조회
  public CursorPageResponse<PopularReviewDto> getPopularReviews(PeriodType period, Sort.Direction direction, String cursor, LocalDateTime after, int limit) {
    PageResult<ReviewRanking> pageData = fetchRankingPageData(
        period, direction, cursor, limit,
        reviewRankingRepository::findLatestBaseDate,
        reviewRankingRepository::countByPeriodTypeAndBaseDate,
        reviewRankingRepository::findByPeriodTypeAndBaseDateAndRankingGreaterThanOrderByRankingAsc,
        reviewRankingRepository::findByPeriodTypeAndBaseDateAndRankingLessThanOrderByRankingDesc
    );

    if (pageData.isEmpty()) return emptyPage();

    List<UUID> reviewIds = pageData.items().stream().map(ReviewRanking::getReviewId).collect(Collectors.toList());
    Map<UUID, Review> reviewMap = reviewRepository.findAllById(reviewIds).stream().collect(Collectors.toMap(Review::getId, r -> r));

    List<PopularReviewDto> content = pageData.items().stream().map(ranking -> {
      Review review = reviewMap.get(ranking.getReviewId());
      Book book = (review != null) ? review.getBook() : null;
      User user = (review != null) ? review.getUser() : null;

      return PopularReviewDto.builder()
          .id(ranking.getId())
          .reviewId(ranking.getReviewId())
          .period(ranking.getPeriodType())
          .rank(ranking.getRanking())
          .score(ranking.getScore())
          .createdAt(ranking.getCreatedAt())
          .bookId(book != null ? book.getId() : null)
          .bookTitle(book != null ? book.getTitle() : "삭제된 도서")
          .bookThumbnailUrl(book != null ? book.getThumbnailUrl() : null)
          .userId(user != null ? user.getId() : null)
          .userNickname(user != null ? user.getNickname() : "알 수 없음")
          .reviewContent(review != null ? review.getContent() : "삭제된 리뷰")
          .reviewRating(review != null ? review.getRating() : 0.0)
          .likeCount(ranking.getLikeCount())
          .commentCount(ranking.getCommentCount())
          .build();
    }).collect(Collectors.toList());

    return createCursorPageResponse(content, pageData.hasNext(), pageData.totalElements(), PopularReviewDto::rank, PopularReviewDto::createdAt);
  }

  // 파워 유저 목록 조회
  public CursorPageResponse<PowerUserDto> getPowerUsers(PeriodType period, Sort.Direction direction, String cursor, LocalDateTime after, int limit) {
    PageResult<UserRanking> pageData = fetchRankingPageData(
        period, direction, cursor, limit,
        userRankingRepository::findLatestBaseDate,
        userRankingRepository::countByPeriodTypeAndBaseDate,
        userRankingRepository::findByPeriodTypeAndBaseDateAndRankingGreaterThanOrderByRankingAsc,
        userRankingRepository::findByPeriodTypeAndBaseDateAndRankingLessThanOrderByRankingDesc
    );

    if (pageData.isEmpty()) return emptyPage();

    List<UUID> userIds = pageData.items().stream().map(UserRanking::getUserId).collect(Collectors.toList());
    Map<UUID, User> userMap = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    List<PowerUserDto> content = pageData.items().stream().map(ranking -> {
      User user = userMap.get(ranking.getUserId());
      return PowerUserDto.builder()
          .userId(ranking.getUserId())
          .nickname(user != null ? user.getNickname() : "탈퇴한 사용자")
          .period(ranking.getPeriodType())
          .rank(ranking.getRanking())
          .score(ranking.getScore())
          .reviewScoreSum(ranking.getReviewScoreSum())
          .likeCount(ranking.getLikeCount())
          .commentCount(ranking.getCommentCount())
          .createdAt(ranking.getCreatedAt())
          .build();
    }).collect(Collectors.toList());

    return createCursorPageResponse(content, pageData.hasNext(), pageData.totalElements(), PowerUserDto::rank, PowerUserDto::createdAt);
  }
}