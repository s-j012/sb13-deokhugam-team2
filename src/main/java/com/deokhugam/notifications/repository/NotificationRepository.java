package com.deokhugam.notifications.repository;

import com.deokhugam.notifications.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  // 도서 물리 삭제 시 연관 알림 일괄 삭제
  void deleteAllByReviewId(UUID reviewId);

}
