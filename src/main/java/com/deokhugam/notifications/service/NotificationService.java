package com.deokhugam.notifications.service;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.notifications.dto.request.NotificationUpdateRequest;
import com.deokhugam.notifications.dto.response.NotificationDto;
import com.deokhugam.notifications.entity.Notification;
import com.deokhugam.notifications.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public NotificationDto readNotification(UUID notificationId, UUID userId, NotificationUpdateRequest request) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(()-> new DeokhugamException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getUser().getId().equals(userId)) {
      throw new DeokhugamException(ErrorCode.FORBIDDEN); // 403
    }

    notification.updateConfirmStatus(request.confirmed());

    return new NotificationDto(
        notification.getId(),
        notification.getUser().getId(),
        notification.getReview().getId(),
        notification.getReview().getContent(),
        notification.getContent(),
        notification.isConfirmed(),
        notification.getConfirmedAt(),
        notification.getCreatedAt(),
        notification.getUpdatedAt(),
        notification.getType()
    );
  }

  @Transactional
  public void readAllNotification(UUID userId){
    List<Notification> unreadNotificarion = notificationRepository.findAllByUserIdAndIsConfirmedFalse(userId);

    // 가져온 알림들을 하나씩 꺼내어 무조건 읽음(true)으로 바꿉니다.
    for(Notification notifications : unreadNotificarion) {
      notifications.updateConfirmStatus(true);
    }
  }

  @Transactional(readOnly = true) // 데이터 변경이 없으므로 readOnly 속도 최적화!
  public List<NotificationDto> getNotifications(UUID userId, LocalDateTime cursor, int size) {
    // 1. 몇 개(size)를 가져올지 세팅
    PageRequest pageRequest = PageRequest.of(0, size);
    // 2. Repository 호출
    List<Notification> notifications = notificationRepository.findAllByCursor(userId, cursor, pageRequest);
    // 3. Entity 목록을 DTO 목록으로 변환(Mapping)해서 반환
    return notifications.stream()
        .map(notification -> new NotificationDto(
            notification.getId(),
            notification.getUser().getId(),
            notification.getReview().getId(),
            notification.getReview().getContent(),
            notification.getContent(),
            notification.isConfirmed(),
            notification.getConfirmedAt(),
            notification.getCreatedAt(),
            notification.getUpdatedAt(),
            notification.getType()
        ))
        .toList(); // 자바 16 이상 최신 문법!
  }

}
