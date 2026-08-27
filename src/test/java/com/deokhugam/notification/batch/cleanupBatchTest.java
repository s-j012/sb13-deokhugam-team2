package com.deokhugam.notification.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.deokhugam.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupBatchTest {
  @Mock
  private NotificationRepository notificationRepository;
  @InjectMocks
  private NotificationCleanupBatch batch;
  @Test
  @DisplayName("30일 지난 알림 삭제 쿼리가 정상적으로 호출되어야 한다")
  void cleanupOldNotifications_Success() {
    // given: Repository가 5개를 지웠다고 흉내 냅니다.
    given(notificationRepository.deleteAllByCreatedAtBefore(any())).willReturn(5);

    // when: 스케줄러 메서드를 강제로 실행해 봅니다.
    batch.cleanupOldNotifications();

    // then: Repository의 삭제 메서드가 딱 1번 잘 호출되었는지 검증합니다!
    verify(notificationRepository, times(1)).deleteAllByCreatedAtBefore(any());
  }
}