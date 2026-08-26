package com.deokhugam.notification.controller;

import com.deokhugam.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.notification.dto.response.NotificationDto;
import com.deokhugam.notification.dto.response.NotificationListResponse;
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

  @PatchMapping("/read-all")
  public ResponseEntity<Void> readAllNotification(
      @RequestHeader ("Deokhugam-Request-User-ID") UUID userId
  ) {

    notificationService.readAllNotification(userId);
    return ResponseEntity.ok().build();
  }

  @GetMapping
  public ResponseEntity<NotificationListResponse> getNotifications(
      @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
      @RequestParam(required = false, defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor, // 처음 요청 시엔 null일 수 있으므로 false
      @RequestParam(required = false) LocalDateTime after,
      @RequestParam(required = false, defaultValue = "10") int limit // 안 보내면 기본값 10개
  ) {
    NotificationListResponse result = notificationService.getNotifications(userId, direction, cursor, after, limit);
    return ResponseEntity.ok(result);
  }
}
