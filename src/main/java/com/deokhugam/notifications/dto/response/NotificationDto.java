package com.deokhugam.notifications.dto.response;

import com.deokhugam.notifications.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID review,
    String content,
    LocalDateTime confirmedAt,
    LocalDateTime createdAt,
    NotificationType type,
    boolean isConfirmed
) { }
