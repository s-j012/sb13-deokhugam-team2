package com.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.not;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.notifications.dto.response.NotificationDto;
import com.deokhugam.notifications.entity.Notification;
import com.deokhugam.notifications.entity.NotificationType;
import com.deokhugam.notifications.repository.NotificationRepository;
import com.deokhugam.notifications.service.NotificationService;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.validator.cfg.defs.UUIDDef;
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
  @DisplayName("단일 알림 읽음 처리 성공 테스트 - isConfirmed가 true로 변경되어야 한다.")
  void readNotification_Success() {
    //given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    User mockUser = mock(User.class);
    given(mockUser.getId()).willReturn(userId);

    Review mockReview = mock(Review.class);
    given(mockReview.getId()).willReturn(UUID.randomUUID());

    Notification mockNotification = Notification.builder()
        .content("테스트용 좋아요 알림")
        .type(NotificationType.REVIEW_LIKE)
        .user(mockUser)
        .review(mockReview)
        .build();

    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));

    //when
    NotificationDto result = notificationService.readNotification(notificationId, userId);

    //then
    assertThat(result).isNotNull();
    assertThat(mockNotification.isConfirmed()).isTrue();
    assertThat(mockNotification.getConfirmedAt()).isNotNull();
  }

  @Test
  @DisplayName("단일 알림 읽음 처리 실패 - 존재하지 않는 ID면 DeokhugamException이 발생해야 한다.")
  void readNotification_Fail_NotFound() {
    //given
    UUID wrongId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    given(notificationRepository.findById(wrongId))
        .willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(()-> notificationService.readNotification(wrongId, userId))
        .isInstanceOf(DeokhugamException.class);
  }

  @Test
  @DisplayName("단일 알림 읽음 처리 실패 - 다른 사용자의 알림 접근 시 DeokhugamException 발생")
  void readNotification_Fail_Forbidden() {
    //given
    UUID ownerId = UUID.randomUUID(); //알림의 실제 소유자
    UUID requesterId = UUID.randomUUID(); //요청한 다른 사용자
    UUID notificationId = UUID.randomUUID();

    User mockUser = mock(User.class);
    given(mockUser.getId()).willReturn(ownerId);

    Notification mockNotification = Notification.builder()
        .content("테스트용 댓글 알림")
        .type(NotificationType.NEW_COMMENT)
        .user(mockUser)
        .build();

    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));

    //when & then
    assertThatThrownBy(() ->
        notificationService.readNotification(notificationId, requesterId))
        .isInstanceOf(DeokhugamException.class);
  }

  @Test
  @DisplayName("이미 읽은 알림을 다시 읽음 처리해도 confirmedAt이 갱신되지 않아야 한다.")
  void readNotification_AlreadyRead_ConfirmedAtUpdated() {
    //given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();

    User mockUser = mock(User.class);
    given(mockUser.getId()).willReturn(userId);

    Review mockReview = mock(Review.class);
    given(mockReview.getId()).willReturn(UUID.randomUUID());

    Notification mockNotification = Notification.builder()
        .content("테스트용 알림")
        .type(NotificationType.REVIEW_LIKE)
        .user(mockUser)
        .review(mockReview)
        .build();

    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));

    //when - 첫 번째 읽음 처리
    notificationService.readNotification(notificationId, userId);
    LocalDateTime firstConfirmedAt = mockNotification.getConfirmedAt(); //최초 기록 시간 저장

    //when - 두 번째 읽음 처리 (중복 요청)
    notificationService.readNotification(notificationId, userId);

    //then - confirmedAt이 첫 번째 시간과 동일해야 통과(갱신 안됨)
    assertThat(mockNotification.getConfirmedAt()).isEqualTo(firstConfirmedAt);
  }
}
