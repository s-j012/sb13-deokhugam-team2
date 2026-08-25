package com.deokhugam.notifications.repository;

import com.deokhugam.notifications.entity.Notification;
import java.util.List;
import java.util.UUID;
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

}
