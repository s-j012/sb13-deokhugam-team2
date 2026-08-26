package com.deokhugam.notification.controller;

import com.deokhugam.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.notification.dto.response.NotificationDto;
import com.deokhugam.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> readNotification(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID")UUID userId,
      @RequestBody NotificationUpdateRequest request
      ) {
    NotificationDto result = notificationService.readNotification(notificationId, userId, request);
    return ResponseEntity.ok(result);
  }

  @PatchMapping
  public ResponseEntity<Void> readAllNotification(
      @RequestHeader ("Deokhugam-Request-User-ID") UUID userId
  ) {

    notificationService.readAllNotification(userId);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<List<NotificationDto>> getNotifications(
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @RequestParam(required = false) LocalDateTime cursor, // 처음 요청 시엔 null일 수 있으므로 false
      @RequestParam(defaultValue = "10") int size // 안 보내면 기본값 10개
  ) {
    List<NotificationDto> result = notificationService.getNotifications(userId, cursor, size);
    return ResponseEntity.ok(result);
  }
}
