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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

  private final BookRankingRepository bookRankingRepository;
  private final ReviewRankingRepository reviewRankingRepository;
  private final UserRankingRepository userRankingRepository;

  private final BookRepository bookRepository;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  private record PagingInfo<T>(List<T> items, boolean hasNext, int startIndex) {}

  private <T> PagingInfo<T> getMemoryPagedList(List<T> allItems, String cursor, int limit) {
    int startIndex = (cursor != null && !cursor.isEmpty()) ? Integer.parseInt(cursor) : 0;
    int endIndex = Math.min(startIndex + limit + 1, allItems.size());

    List<T> pagedItems = allItems.subList(startIndex, endIndex);
    boolean hasNext = pagedItems.size() > limit;

    if (hasNext) {
      pagedItems = pagedItems.subList(0, limit);
    }
    return new PagingInfo<>(pagedItems, hasNext, startIndex);
  }

  private <T> CursorPageResponse<T> createCursorPageResponse(
      PagingInfo<?> paging,
      List<T> content,
      long totalElements,
      int limit,
      Function<T, LocalDateTime> dateExtractor) {

    String nextCursor = (paging.hasNext() && !content.isEmpty()) ? String.valueOf(paging.startIndex() + limit) : null;
    LocalDateTime nextAfter = (paging.hasNext() && !content.isEmpty()) ? dateExtractor.apply(content.get(content.size() - 1)) : null;

    return new CursorPageResponse<>(content, nextCursor, nextAfter, content.size(), totalElements, paging.hasNext());
  }

  // 인기 도서 목록 조회
  public CursorPageResponse<PopularBookDto> getPopularBooks(PeriodType period, String direction, String cursor, String after, int limit) {
    LocalDate today = LocalDate.now();
    List<BookRanking> allRankings = bookRankingRepository.findAllByPeriodTypeAndBaseDateOrderByRankingAsc(period, today);

    PagingInfo<BookRanking> paging = getMemoryPagedList(allRankings, cursor, limit);

    List<UUID> bookIds = paging.items().stream().map(BookRanking::getBookId).collect(Collectors.toList());
    Map<UUID, Book> bookMap = bookRepository.findAllById(bookIds).stream().collect(Collectors.toMap(Book::getId, b -> b));

    List<PopularBookDto> content = paging.items().stream().map(ranking -> {
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

    return createCursorPageResponse(paging, content, allRankings.size(), limit, PopularBookDto::createdAt);
  }

  // 인기 리뷰 목록 조회
  public CursorPageResponse<PopularReviewDto> getPopularReviews(PeriodType period, String direction, String cursor, String after, int limit) {
    LocalDate today = LocalDate.now();
    List<ReviewRanking> allRankings = reviewRankingRepository.findAllByPeriodTypeAndBaseDateOrderByRankingAsc(period, today);

    PagingInfo<ReviewRanking> paging = getMemoryPagedList(allRankings, cursor, limit);

    List<UUID> reviewIds = paging.items().stream().map(ReviewRanking::getReviewId).collect(Collectors.toList());
    Map<UUID, Review> reviewMap = reviewRepository.findAllById(reviewIds).stream().collect(Collectors.toMap(Review::getId, r -> r));

    List<PopularReviewDto> content = paging.items().stream().map(ranking -> {
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

    return createCursorPageResponse(paging, content, allRankings.size(), limit, PopularReviewDto::createdAt);
  }

  // 파워 유저 목록 조회
  public CursorPageResponse<PowerUserDto> getPowerUsers(PeriodType period, String direction, String cursor, String after, int limit) {
    LocalDate today = LocalDate.now();
    List<UserRanking> allRankings = userRankingRepository.findAllByPeriodTypeAndBaseDateOrderByRankingAsc(period, today);

    PagingInfo<UserRanking> paging = getMemoryPagedList(allRankings, cursor, limit);

    List<UUID> userIds = paging.items().stream().map(UserRanking::getUserId).collect(Collectors.toList());
    Map<UUID, User> userMap = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

    List<PowerUserDto> content = paging.items().stream().map(ranking -> {
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

    return createCursorPageResponse(paging, content, allRankings.size(), limit, PowerUserDto::createdAt);
  }
}