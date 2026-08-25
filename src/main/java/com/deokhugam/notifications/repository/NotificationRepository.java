package com.deokhugam.notifications.repository;

import com.deokhugam.notifications.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  @Modifying
  @Query("delete from Notification n where n.review.id = :reviewId")
  void deleteAllByReviewId(@Param("reviewId") UUID reviewId);

  //user의 안읽은(isConfirmed=false) 알림만 모두 가져오는 퀴리 메서드
  List<Notification> findAllByUserIdAndIsConfirmedFalse(UUID userId);

  // 커서(시간) 기준 다음 페이지 목록 조회 (최신순 정렬)
  @Query("select n from Notification n where n.user.id = :userId " +
      "and (:cursor is null or n.createdAt < :cursor) " +
      "order by n.createdAt desc")
  List<Notification> findAllByCursor(
      @Param("userId") UUID userId,
      @Param("cursor") LocalDateTime cursor,
      Pageable pageable
  );
}
