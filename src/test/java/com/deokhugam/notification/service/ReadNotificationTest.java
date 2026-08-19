package com.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.notifications.entity.Notification;
import com.deokhugam.notifications.entity.NotificationType;
import com.deokhugam.notifications.repository.NotificationRepository;
import com.deokhugam.notifications.service.NotificationService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReadNotificationTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  @Test
  @DisplayName("알림 읽음 처리 성공 테스트")
  void readNotification_Success() {
    //given
    UUID id = UUID.randomUUID();
    Notification mockNotification = Notification.builder()
        .content("테스트용 좋아요 알림")
        .type(NotificationType.REVIEW_LIKE)
        .build();

    given(notificationRepository.findById(id))
        .willReturn(Optional.of(mockNotification));

    //when
    notificationService.readNotification(id);

    //then
    assertThat(mockNotification.isConfirmed()).isTrue();
    assertThat(mockNotification.getConfirmedAt()).isNotNull();
  }

  @Test
  @DisplayName("알림 읽음 처리 실패 - 존재하지 않는 ID면 DeokhugamException이 발생해야 한다.")
  void readNotification_Fail_NotFound() {
    //given
    UUID wrongId = UUID.randomUUID();

    given(notificationRepository.findById(wrongId))
        .willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(()-> notificationService.readNotification(wrongId))
        .isInstanceOf(DeokhugamException.class);
  }
}
