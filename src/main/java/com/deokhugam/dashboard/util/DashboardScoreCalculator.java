package com.deokhugam.dashboard.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DashboardScoreCalculator {

  // 도서 점수 가중치
  private static final double BOOK_REVIEW_WEIGHT = 0.4;
  private static final double BOOK_RATING_WEIGHT = 0.6;

  // 리뷰 점수 가중치
  private static final double REVIEW_LIKE_WEIGHT = 0.3;
  private static final double REVIEW_COMMENT_WEIGHT = 0.7;

  // 파워 유저 점수 가중치
  private static final double USER_REVIEW_SCORE_WEIGHT = 0.5;
  private static final double USER_LIKE_WEIGHT = 0.2;
  private static final double USER_COMMENT_WEIGHT = 0.3;

  // 랭킹 계산 점수의 소수점 자릿수
  private static final int SCORE_SCALE = 4;


  private DashboardScoreCalculator() {
  }

  // 인기 도서 점수
  public static double calculateBookScore(long reviewCount, double averageRating) {
    double rawScore = (reviewCount * BOOK_REVIEW_WEIGHT) + (averageRating * BOOK_RATING_WEIGHT);
    return roundScore(rawScore);
  }

  // 인기 리뷰 점수
  public static double calculateReviewScore(long likeCount, long commentCount) {
    double rawScore = (likeCount * REVIEW_LIKE_WEIGHT) + (commentCount * REVIEW_COMMENT_WEIGHT);
    return roundScore(rawScore);
  }

  // 파워 유저 점수
  public static double calculatePowerUserScore(double reviewScoreSum, long likeCount, long commentCount) {
    double rawScore = (reviewScoreSum * USER_REVIEW_SCORE_WEIGHT) +
        (likeCount * USER_LIKE_WEIGHT) +
        (commentCount * USER_COMMENT_WEIGHT);
    return roundScore(rawScore);
  }

  private static double roundScore(double value) {
    return BigDecimal.valueOf(value)
        .setScale(SCORE_SCALE, RoundingMode.HALF_UP)
        .doubleValue();
  }
}