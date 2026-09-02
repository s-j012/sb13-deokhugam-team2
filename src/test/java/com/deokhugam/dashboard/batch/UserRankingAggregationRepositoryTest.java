package com.deokhugam.dashboard.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import com.deokhugam.dashboard.batch.UserRankingAggregationRepository.UserAggregation;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.entity.ReviewLike;
import com.deokhugam.review.repository.ReviewLikeRepository;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
    JpaConfig.class,
    UserRankingAggregationRepository.class
})
class UserRankingAggregationRepositoryTest {

  @Autowired
  private UserRankingAggregationRepository aggregationRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private ReviewLikeRepository reviewLikeRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Test
  @DisplayName("리뷰, 좋아요, 댓글 활동을 유저별로 집계한다")
  void aggregateUserActivities() {
    User author =
        userRepository.save(createUser());

    User participant =
        userRepository.save(createUser());

    Book book =
        bookRepository.save(createBook());

    Review review = reviewRepository.save(
        Review.create(
            author,
            book,
            "파워 유저 테스트 리뷰",
            5
        )
    );

    reviewLikeRepository.save(
        ReviewLike.create(
            review,
            participant
        )
    );

    commentRepository.save(
        new Comment(
            "활성 댓글",
            participant.getId(),
            review.getId()
        )
    );

    Comment deletedComment = commentRepository.save(
        new Comment(
            "삭제된 댓글",
            participant.getId(),
            review.getId()
        )
    );

    deletedComment.softDelete();
    review.softDelete();

    reviewRepository.flush();
    reviewLikeRepository.flush();
    commentRepository.flush();

    LocalDateTime now = LocalDateTime.now();

    PeriodRange periodRange = new PeriodRange(
        now.minusMinutes(1),
        now.plusMinutes(1)
    );

    List<UserAggregation> result =
        aggregationRepository.aggregate(periodRange);

    assertThat(result).hasSize(2);

    Map<UUID, UserAggregation> aggregationMap =
        result.stream()
            .collect(Collectors.toMap(
                UserAggregation::userId,
                Function.identity()
            ));

    UserAggregation authorAggregation =
        aggregationMap.get(author.getId());

    assertThat(authorAggregation)
        .isNotNull();

    assertThat(authorAggregation.reviewLikeCount())
        .isEqualTo(1L);

    assertThat(authorAggregation.reviewCommentCount())
        .isEqualTo(2L);

    assertThat(authorAggregation.likeCount())
        .isZero();

    assertThat(authorAggregation.commentCount())
        .isZero();

    UserAggregation participantAggregation =
        aggregationMap.get(participant.getId());

    assertThat(participantAggregation)
        .isNotNull();

    assertThat(participantAggregation.reviewLikeCount())
        .isZero();

    assertThat(participantAggregation.reviewCommentCount())
        .isZero();

    assertThat(participantAggregation.likeCount())
        .isEqualTo(1L);

    assertThat(participantAggregation.commentCount())
        .isEqualTo(2L);
  }

  @Test
  @DisplayName("기간 밖의 활동은 파워 유저 집계에서 제외한다")
  void excludeActivitiesOutsidePeriod() {
    User user =
        userRepository.save(createUser());

    Book book =
        bookRepository.save(createBook());

    reviewRepository.save(
        Review.create(
            user,
            book,
            "기간 밖 리뷰",
            5
        )
    );

    reviewRepository.flush();

    LocalDateTime now = LocalDateTime.now();

    PeriodRange periodRange = new PeriodRange(
        now.plusDays(1),
        now.plusDays(2)
    );

    List<UserAggregation> result =
        aggregationRepository.aggregate(periodRange);

    assertThat(result)
        .isEmpty();
  }

  private User createUser() {
    return User.create(
        "batch-" + UUID.randomUUID() + "@example.com",
        "batchUser",
        "encodedPassword"
    );
  }

  private Book createBook() {
    return new Book(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 26),
        null
    );
  }
}