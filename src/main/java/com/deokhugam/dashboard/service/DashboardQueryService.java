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
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  private RankingCursor parseRankingCursor(String cursor, LocalDateTime after) {
    boolean hasCursor = cursor != null && !cursor.isBlank();
    boolean hasAfter = after != null;

    if (hasCursor != hasAfter) {
      throw new InvalidDashboardPaginationException(
          "cursor와 after는 둘 다 전달하거나 둘 다 생략해야 합니다."
      );
    }

    if (!hasCursor) {
      return new RankingCursor(null, null);
    }

    try {
      long ranking = Long.parseLong(cursor.trim());
      if (ranking < 0) {
        throw new InvalidDashboardPaginationException("cursor는 0 이상의 순위여야 합니다.");
      }
      return new RankingCursor(ranking, after);
    } catch (NumberFormatException e) {
      throw new InvalidDashboardPaginationException(
          "cursor는 숫자(순위) 형식이어야 합니다.",
          e
      );
    }
  }

  private void validateRequest(PeriodType period, Sort.Direction direction, int limit) {
    if (period == null) {
      throw new InvalidDashboardPaginationException("period는 필수입니다.");
    }
    if (direction == null) {
      throw new InvalidDashboardPaginationException("direction은 필수입니다.");
    }
    if (limit <= 0) {
      throw new InvalidDashboardPaginationException("limit는 1 이상이어야 합니다.");
    }
    if (limit == Integer.MAX_VALUE) {
      throw new InvalidDashboardPaginationException("limit가 너무 큽니다.");
    }
  }

  private <T> CursorPageResponse<T> createCursorPageResponse(
      List<T> content,
      boolean hasNext,
      long totalElements,
      Function<T, Long> rankExtractor,
      Function<T, LocalDateTime> dateExtractor
  ) {
    String nextCursor = null;
    LocalDateTime nextAfter = null;

    if (hasNext && !content.isEmpty()) {
      T lastItem = content.get(content.size() - 1);
      nextCursor = String.valueOf(rankExtractor.apply(lastItem));
      nextAfter = dateExtractor.apply(lastItem);
    }

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  private <T> PageResult<T> fetchRankingPageData(
      PeriodType period,
      Sort.Direction direction,
      String cursor,
      LocalDateTime after,
      int limit,
      Function<PeriodType, Optional<LocalDate>> baseDateFetcher,
      BiFunction<PeriodType, LocalDate, Long> countFetcher,
      RankingQueryFunction<T> ascQuery,
      RankingQueryFunction<T> descQuery
  ) {
    validateRequest(period, direction, limit);
    RankingCursor rankingCursor = parseRankingCursor(cursor, after);

    Optional<LocalDate> optionalBaseDate = baseDateFetcher.apply(period);
    if (optionalBaseDate.isEmpty()) {
      return new PageResult<>(List.of(), 0L, false);
    }

    LocalDate baseDate = optionalBaseDate.get();
    long totalElements = countFetcher.apply(period, baseDate);
    Pageable pageable = PageRequest.of(0, limit + 1);

    List<T> fetched = direction.isAscending()
        ? ascQuery.execute(
        period,
        baseDate,
        rankingCursor.ranking(),
        rankingCursor.after(),
        pageable
    )
        : descQuery.execute(
            period,
            baseDate,
            rankingCursor.ranking(),
            rankingCursor.after(),
            pageable
        );

    boolean hasNext = fetched.size() > limit;
    List<T> pageItems = hasNext ? fetched.subList(0, limit) : fetched;

    return new PageResult<>(pageItems, totalElements, hasNext);
  }

  // 인기 도서 목록 조회
  public CursorPageResponse<PopularBookDto> getPopularBooks(
      PeriodType period,
      Sort.Direction direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {
    PageResult<BookRanking> pageData = fetchRankingPageData(
        period,
        direction,
        cursor,
        after,
        limit,
        bookRankingRepository::findLatestBaseDate,
        bookRankingRepository::countByPeriodTypeAndBaseDate,
        bookRankingRepository::findRankingPageAsc,
        bookRankingRepository::findRankingPageDesc
    );

    List<UUID> bookIds = pageData.items().stream()
        .map(BookRanking::getBookId)
        .distinct()
        .collect(Collectors.toList());

    Map<UUID, Book> bookMap = bookRepository.findAllById(bookIds).stream()
        .collect(Collectors.toMap(Book::getId, Function.identity()));

    List<PopularBookDto> content = pageData.items().stream()
        .map(ranking -> {
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
        })
        .collect(Collectors.toList());

    return createCursorPageResponse(
        content,
        pageData.hasNext(),
        pageData.totalElements(),
        PopularBookDto::rank,
        PopularBookDto::createdAt
    );
  }

  // 인기 리뷰 목록 조회
  public CursorPageResponse<PopularReviewDto> getPopularReviews(
      PeriodType period,
      Sort.Direction direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {
    PageResult<ReviewRanking> pageData = fetchRankingPageData(
        period,
        direction,
        cursor,
        after,
        limit,
        reviewRankingRepository::findLatestBaseDate,
        reviewRankingRepository::countByPeriodTypeAndBaseDate,
        reviewRankingRepository::findRankingPageAsc,
        reviewRankingRepository::findRankingPageDesc
    );

    List<UUID> reviewIds = pageData.items().stream()
        .map(ReviewRanking::getReviewId)
        .distinct()
        .collect(Collectors.toList());

    Map<UUID, Review> reviewMap = reviewRepository.findAllById(reviewIds).stream()
        .collect(Collectors.toMap(Review::getId, Function.identity()));

    List<PopularReviewDto> content = pageData.items().stream()
        .map(ranking -> {
          Review review = reviewMap.get(ranking.getReviewId());
          Book book = review != null ? review.getBook() : null;
          User user = review != null ? review.getUser() : null;

          return PopularReviewDto.builder()
              .id(ranking.getId())
              .reviewId(ranking.getReviewId())
              .bookId(book != null ? book.getId() : null)
              .bookTitle(book != null ? book.getTitle() : "삭제된 도서")
              .bookThumbnailUrl(book != null ? book.getThumbnailUrl() : null)
              .userId(user != null ? user.getId() : null)
              .userNickname(user != null ? user.getNickname() : "알 수 없음")
              .reviewContent(review != null ? review.getContent() : "삭제된 리뷰")
              .reviewRating(review != null ? review.getRating() : 0.0)
              .period(ranking.getPeriodType())
              .rank(ranking.getRanking())
              .score(ranking.getScore())
              .likeCount(ranking.getLikeCount())
              .commentCount(ranking.getCommentCount())
              .createdAt(ranking.getCreatedAt())
              .build();
        })
        .collect(Collectors.toList());

    return createCursorPageResponse(
        content,
        pageData.hasNext(),
        pageData.totalElements(),
        PopularReviewDto::rank,
        PopularReviewDto::createdAt
    );
  }

  // 파워 유저 목록 조회
  public CursorPageResponse<PowerUserDto> getPowerUsers(
      PeriodType period,
      Sort.Direction direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {
    PageResult<UserRanking> pageData = fetchRankingPageData(
        period,
        direction,
        cursor,
        after,
        limit,
        userRankingRepository::findLatestBaseDate,
        userRankingRepository::countByPeriodTypeAndBaseDate,
        userRankingRepository::findRankingPageAsc,
        userRankingRepository::findRankingPageDesc
    );

    List<UUID> userIds = pageData.items().stream()
        .map(UserRanking::getUserId)
        .distinct()
        .collect(Collectors.toList());

    Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(User::getId, Function.identity()));

    List<PowerUserDto> content = pageData.items().stream()
        .map(ranking -> {
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
        })
        .collect(Collectors.toList());

    return createCursorPageResponse(
        content,
        pageData.hasNext(),
        pageData.totalElements(),
        PowerUserDto::rank,
        PowerUserDto::createdAt
    );
  }

  private record RankingCursor(Long ranking, LocalDateTime after) {
  }

  private record PageResult<T>(List<T> items, long totalElements, boolean hasNext) {
  }

  @FunctionalInterface
  private interface RankingQueryFunction<T> {

    List<T> execute(
        PeriodType period,
        LocalDate baseDate,
        Long ranking,
        LocalDateTime after,
        Pageable pageable
    );
  }
}
