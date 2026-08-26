package com.deokhugam.notification.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationListResponse(
    List<NotificationDto> content, // 실제 알림 목록
    String nextCursor,             // 다음 커서 (시간 문자열)
    LocalDateTime nextAfter,       // 다음 커서 (시간 객체)
    int size,                      // 현재 페이지의 데이터 개수
    long totalElements,            // 전체 알림 개수
    boolean hasNext                // 다음 페이지 존재 여부
) { }
