package com.deokhugam.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.notification.entity.Notification;
import com.deokhugam.notification.entity.NotificationType;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
import com.deokhugam.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class NotificationRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private EntityManager entityManager; //JPA 영속성 컨텍스트를 직접 비우기 위해 사용됨.

  @Autowired
  private com.deokhugam.user.repository.UserRepository userRepository;

  @Autowired
  private com.deokhugam.book.repository.BookRepository bookRepository;

  @Autowired
  private com.deokhugam.review.repository.ReviewRepository reviewRepository;

  @Test
  @DisplayName("리뷰 ID로 해당 리뷰의 알림을 모두 물리 삭제하고 다른 리뷰의 알림은 유지한다")
  void deleteAllByReviewId() {

    // given (테스트용 데이터)
    User user = userRepository.save(
        User.create(
            "notification-test@example.com",
            "notificationUser",
            "password"
        )
    );

    Book bookA = bookRepository.save(
        new Book(
            "도서 A",
            "저자 A",
            "설명 A",
            "출판사 A",
            LocalDate.of(2026, 1, 1),
            "9781234567891"
        )
    );

    Book bookB = bookRepository.save(
        new Book(
            "도서 B",
            "저자 B",
            "설명 B",
            "출판사 B",
            LocalDate.of(2026, 1, 2),
            "9781234567892"
        )
    );

    Review reviewA = reviewRepository.save(
        Review.create(user, bookA, "리뷰 A", 5)
    );

    Review reviewB = reviewRepository.save(
        Review.create(user, bookB, "리뷰 B", 4)
    );

    Notification notificationA1 = Notification.builder()
        .user(user)
        .review(reviewA)
        .content("리뷰 A 알림 1")
        .type(NotificationType.REVIEW_LIKE)
        .build();

    Notification notificationA2 = Notification.builder()
        .user(user)
        .review(reviewA)
        .content("리뷰 A 알림 2")
        .type(NotificationType.NEW_COMMENT)
        .build();

    Notification notificationB = Notification.builder()
        .user(user)
        .review(reviewB)
        .content("리뷰 B 알림")
        .type(NotificationType.REVIEW_LIKE)
        .build();

    notificationRepository.saveAll(
        List.of(notificationA1, notificationA2, notificationB)
    );
    notificationRepository.flush();

    // when
    notificationRepository.deleteAllByReviewId(reviewA.getId());
    notificationRepository.flush();

    entityManager.clear();

    // then
    List<Notification> remaining =
        notificationRepository.findAll();

    assertThat(remaining)
        .hasSize(1);

    assertThat(remaining.get(0).getReview().getId())
        .isEqualTo(reviewB.getId());

    assertThat(remaining)
        .noneMatch(notification ->
            notification.getReview().getId().equals(reviewA.getId()));
  }

  @Test
  @DisplayName("확인한 지 7일이 지난 알림만 삭제되고, 미확인 알림이나 최근 확인 알림은 유지된다")
  void deleteOldConfirmedNotifications() {
    // 1. given (테스트용 데이터 세팅)
    User user = userRepository.save(
        User.create("notification-test2@example.com", "notificationUser2", "password")
    );
    Book bookA = bookRepository.save(
        new Book("도서 A", "저자 A", "설명 A", "출판사 A", LocalDate.of(2026, 1, 1), "9781234567891")
    );
    Review reviewA = reviewRepository.save(
        Review.create(user, bookA, "리뷰 A", 5)
    );
    // 알림 1: 오래된 읽은 알림 (삭제 대상 O)
    Notification oldRead = Notification.builder()
        .user(user).review(reviewA).content("삭제될 알림").type(NotificationType.REVIEW_LIKE).build();
    oldRead.updateConfirmStatus(true);
    // 강제로 10일 전 읽음 처리
    org.springframework.test.util.ReflectionTestUtils.setField(oldRead, "confirmedAt", LocalDateTime.now().minusDays(10));
    // 알림 2: 최근 읽은 알림 (삭제 대상 X)
    Notification recentRead = Notification.builder()
        .user(user).review(reviewA).content("살아남을 알림1").type(NotificationType.REVIEW_LIKE).build();
    recentRead.updateConfirmStatus(true); // 현재 시간 읽음 처리됨 (7일 안 지남)
    // 알림 3: 오래된 안 읽은 알림 (삭제 대상 X)
    Notification oldUnread = Notification.builder()
        .user(user).review(reviewA).content("살아남을 알림2").type(NotificationType.REVIEW_LIKE).build();
    // 생성은 10일 전이지만 읽지 않음 (isConfirmed = false)
    org.springframework.test.util.ReflectionTestUtils.setField(oldUnread, "createdAt", LocalDateTime.now().minusDays(10));
    notificationRepository.saveAll(List.of(oldRead, recentRead, oldUnread));
    notificationRepository.flush();
    // 2. when (삭제 로직 실행)
    // 7일 전을 cutoffDate로 설정하여 쿼리 메서드 호출
    int deletedCount = notificationRepository.deleteOldConfirmedNotifications(LocalDateTime.now().minusDays(7));
    notificationRepository.flush();
    entityManager.clear(); // 1차 캐시 비우기
    // 3. then (결과 검증)
    List<Notification> remaining = notificationRepository.findAll();

    // 삭제된 개수는 딱 1개(oldRead)여야 합니다.
    assertThat(deletedCount).isEqualTo(1);

    // 남은 알림은 2개(recentRead, oldUnread)여야 합니다.
    assertThat(remaining).hasSize(2);
  }

  @Test
  @DisplayName("커서 없이 알림 목록을 최신순(내림차순)으로 조회할 수 있다")
  @SuppressWarnings("SqlResolve")
  void findAllByUserIdDesc() {
    // given
    User user = userRepository.save(User.create("test-cursor@example.com", "testUser", "password"));
    Book book = bookRepository.save(
        new Book("도서 A", "저자 A", "설명 A", "출판사 A", LocalDate.of(2026, 1, 1), "9781234567891"));
    Review review = reviewRepository.save(
        Review.create(user, book, "테스트 리뷰", 5));

    Notification noti1 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림1").type(NotificationType.REVIEW_LIKE).build());
    Notification noti2 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림2").type(NotificationType.NEW_COMMENT).build());

    entityManager.createNativeQuery("UPDATE notifications SET created_at = :time WHERE id = :id")
        .setParameter("time", LocalDateTime.now().minusHours(1))
        .setParameter("id", noti1.getId())
        .executeUpdate();

    entityManager.clear(); // 영속성 컨텍스트를 비워야 다음 조회 시 DB에서 새로 바뀐 값을 읽어옵니다.

    // when: 첫 페이지 조회 (커서 없음)
    org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 10);
    List<Notification> result = notificationRepository.findAllByUserIdDesc(user.getId(), pageRequest);

    // then: 2개가 조회되고, 최신인 noti2가 먼저 나와야 함
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo("알림2");
  }

  @Test
  @DisplayName("커서 없이 알림 목록을 오래된순(오름차순)으로 조회할 수 있다")
  @SuppressWarnings("SqlResolve")
  void findAllByUserIdAsc() {
    // given
    User user = userRepository.save(User.create("test-asc@example.com", "testUser2", "password"));
    Book book = bookRepository.save(new Book("도서 B", "저자 B", "설명 B", "출판사 B", LocalDate.of(2026, 1, 1), "9781234567891"));
    Review review = reviewRepository.save(Review.create(user, book, "테스트 리뷰2", 5));
    Notification noti1 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림1").type(NotificationType.REVIEW_LIKE).build());
    Notification noti2 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림2").type(NotificationType.NEW_COMMENT).build());

    entityManager.createNativeQuery("UPDATE notifications SET created_at = :time WHERE id = :id")
        .setParameter("time", LocalDateTime.now().minusHours(1))
        .setParameter("id", noti1.getId())
        .executeUpdate();

    entityManager.clear(); // 1차 캐시 비우기

    // when: 첫 페이지 조회 (커서 없음, 오름차순)
    org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 10);
    List<Notification> result = notificationRepository.findAllByUserIdAsc(user.getId(), pageRequest);

    // then: 오래된 noti1(1시간 전)이 먼저 나와야 함
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo("알림1");
  }

  @Test
  @DisplayName("생성 시간이 완전히 똑같은 알림들도 복합 커서를 통해 누락 없이 조회할 수 있다")
  @SuppressWarnings("SqlResolve")
  void findAllByCursorDesc_withSameCreatedAt() {
    // given
    User user = userRepository.save(User.create("same-time@example.com", "sameTimeUser", "password"));
    Book book = bookRepository.save(new Book("도서 C", "저자 C", "설명 C", "출판사 C", java.time.LocalDate.of(2026, 1, 1), "9781234567893"));
    Review review = reviewRepository.save(Review.create(user, book, "테스트 리뷰3", 5));

    Notification noti1 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림1").type(NotificationType.REVIEW_LIKE).build());
    Notification noti2 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림2").type(NotificationType.NEW_COMMENT).build());
    Notification noti3 = notificationRepository.save(Notification.builder().user(user).review(review).content("알림3").type(NotificationType.REVIEW_LIKE).build());

    // 해결 1: 밀리초 단위로 잘라내어 DB 저장 시 소수점 이하 시간 차이(나노초)로 인한 버그 방지
    LocalDateTime sameTime = LocalDateTime.now().minusHours(2).truncatedTo(java.time.temporal.ChronoUnit.MILLIS);

    entityManager.createNativeQuery("UPDATE notifications SET created_at = :time WHERE id IN (:id1, :id2, :id3)")
        .setParameter("time", sameTime)
        .setParameter("id1", noti1.getId())
        .setParameter("id2", noti2.getId())
        .setParameter("id3", noti3.getId())
        .executeUpdate();
    entityManager.clear(); // DB 업데이트 후 영속성 컨텍스트 초기화

    // 해결 2: UUID는 랜덤이므로 일단 전체를 조회해서 정렬 결과상 가장 위에 있는(제일 큰 UUID) 알림을 찾습니다.
    org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(0, 10);
    List<Notification> allNotifications = notificationRepository.findAllByUserIdDesc(user.getId(), pageRequest);
    Notification topNotification = allNotifications.get(0); // 1등 (커서로 사용)

    // when: 1등 알림을 커서로 삼아 다음 데이터를 요청
    List<Notification> result = notificationRepository.findAllByCursorDesc(user.getId(), sameTime, topNotification.getId(), pageRequest);

    // then: 첫 번째 알림을 제외한 나머지 2개가 누락 없이 순서대로 조회되어야 함!
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getId()).isEqualTo(allNotifications.get(1).getId());
    assertThat(result.get(1).getId()).isEqualTo(allNotifications.get(2).getId());
  }
}