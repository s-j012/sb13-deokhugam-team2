package com.deokhugam.dashboard.batch;

import com.deokhugam.dashboard.batch.DashboardPeriodResolver.PeriodRange;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRankingAggregationRepository {

  private final EntityManager entityManager;

  public List<UserAggregation> aggregate(PeriodRange periodRange) {
    List<Object[]> reviewRows = findReviews(periodRange);

    List<UUID> reviewIds = reviewRows.stream()
        .map(row -> (UUID) row[0])
        .toList();

    Map<UUID, Long> receivedLikeCountByReview =
        findReceivedLikeCounts(
            reviewIds,
            periodRange.endExclusive()
        );

    Map<UUID, Long> receivedCommentCountByReview =
        findReceivedCommentCounts(
            reviewIds,
            periodRange.endExclusive()
        );

    Map<UUID, Long> reviewLikeCountByUser = new HashMap<>();
    Map<UUID, Long> reviewCommentCountByUser = new HashMap<>();

    for (Object[] row : reviewRows) {
      UUID reviewId = (UUID) row[0];
      UUID userId = (UUID) row[1];

      reviewLikeCountByUser.merge(
          userId,
          receivedLikeCountByReview.getOrDefault(reviewId, 0L),
          Long::sum
      );

      reviewCommentCountByUser.merge(
          userId,
          receivedCommentCountByReview.getOrDefault(reviewId, 0L),
          Long::sum
      );
    }

    Map<UUID, Long> likeCountByUser =
        findParticipatedLikeCounts(periodRange);

    Map<UUID, Long> commentCountByUser =
        findParticipatedCommentCounts(periodRange);

    Set<UUID> userIds = new HashSet<>();

    userIds.addAll(reviewLikeCountByUser.keySet());
    userIds.addAll(reviewCommentCountByUser.keySet());
    userIds.addAll(likeCountByUser.keySet());
    userIds.addAll(commentCountByUser.keySet());

    return userIds.stream()
        .map(userId -> new UserAggregation(
            userId,
            reviewLikeCountByUser.getOrDefault(userId, 0L),
            reviewCommentCountByUser.getOrDefault(userId, 0L),
            likeCountByUser.getOrDefault(userId, 0L),
            commentCountByUser.getOrDefault(userId, 0L)
        ))
        .toList();
  }

  private List<Object[]> findReviews(
      PeriodRange periodRange
  ) {
    TypedQuery<Object[]> query;

    if (periodRange.startInclusive() == null) {
      query = entityManager.createQuery(
          """
          SELECT r.id, r.user.id
          FROM Review r
          WHERE r.createdAt < :endExclusive
          """,
          Object[].class
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    } else {
      query = entityManager.createQuery(
          """
          SELECT r.id, r.user.id
          FROM Review r
          WHERE r.createdAt >= :startInclusive
            AND r.createdAt < :endExclusive
          """,
          Object[].class
      );

      query.setParameter(
          "startInclusive",
          periodRange.startInclusive()
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    }

    return query.getResultList();
  }

  private Map<UUID, Long> findReceivedLikeCounts(
      List<UUID> reviewIds,
      LocalDateTime endExclusive
  ) {
    if (reviewIds.isEmpty()) {
      return Map.of();
    }

    TypedQuery<Object[]> query = entityManager.createQuery(
        """
        SELECT rl.review.id, COUNT(rl)
        FROM ReviewLike rl
        WHERE rl.review.id IN :reviewIds
          AND rl.createdAt < :endExclusive
        GROUP BY rl.review.id
        """,
        Object[].class
    );

    query.setParameter(
        "reviewIds",
        reviewIds
    );

    query.setParameter(
        "endExclusive",
        endExclusive
    );

    return toCountMap(query.getResultList());
  }

  private Map<UUID, Long> findReceivedCommentCounts(
      List<UUID> reviewIds,
      LocalDateTime endExclusive
  ) {
    if (reviewIds.isEmpty()) {
      return Map.of();
    }

    TypedQuery<Object[]> query = entityManager.createQuery(
        """
        SELECT c.reviewId, COUNT(c)
        FROM Comment c
        WHERE c.reviewId IN :reviewIds
          AND c.createdAt < :endExclusive
        GROUP BY c.reviewId
        """,
        Object[].class
    );

    query.setParameter(
        "reviewIds",
        reviewIds
    );

    query.setParameter(
        "endExclusive",
        endExclusive
    );

    return toCountMap(query.getResultList());
  }

  private Map<UUID, Long> findParticipatedLikeCounts(
      PeriodRange periodRange
  ) {
    TypedQuery<Object[]> query;

    if (periodRange.startInclusive() == null) {
      query = entityManager.createQuery(
          """
          SELECT rl.user.id, COUNT(rl)
          FROM ReviewLike rl
          WHERE rl.createdAt < :endExclusive
          GROUP BY rl.user.id
          """,
          Object[].class
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    } else {
      query = entityManager.createQuery(
          """
          SELECT rl.user.id, COUNT(rl)
          FROM ReviewLike rl
          WHERE rl.createdAt >= :startInclusive
            AND rl.createdAt < :endExclusive
          GROUP BY rl.user.id
          """,
          Object[].class
      );

      query.setParameter(
          "startInclusive",
          periodRange.startInclusive()
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    }

    return toCountMap(query.getResultList());
  }

  private Map<UUID, Long> findParticipatedCommentCounts(
      PeriodRange periodRange
  ) {
    TypedQuery<Object[]> query;

    if (periodRange.startInclusive() == null) {
      query = entityManager.createQuery(
          """
          SELECT c.userId, COUNT(c)
          FROM Comment c
          WHERE c.createdAt < :endExclusive
          GROUP BY c.userId
          """,
          Object[].class
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    } else {
      query = entityManager.createQuery(
          """
          SELECT c.userId, COUNT(c)
          FROM Comment c
          WHERE c.createdAt >= :startInclusive
            AND c.createdAt < :endExclusive
          GROUP BY c.userId
          """,
          Object[].class
      );

      query.setParameter(
          "startInclusive",
          periodRange.startInclusive()
      );

      query.setParameter(
          "endExclusive",
          periodRange.endExclusive()
      );
    }

    return toCountMap(query.getResultList());
  }

  private Map<UUID, Long> toCountMap(
      List<Object[]> rows
  ) {
    Map<UUID, Long> result = new HashMap<>();

    for (Object[] row : rows) {
      result.put(
          (UUID) row[0],
          ((Number) row[1]).longValue()
      );
    }

    return result;
  }

  public record UserAggregation(
      UUID userId,
      long reviewLikeCount,
      long reviewCommentCount,
      long likeCount,
      long commentCount
  ) {
  }
}