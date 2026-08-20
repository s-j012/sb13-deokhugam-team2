package com.deokhugam.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.entity.Book;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class BookRepositoryTest {

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("여러 도서 조회 시 논리 삭제된 도서를 제외한다")
  void findActiveBooks() {
    Book activeBook = bookRepository.save(
        createBook()
    );

    Book deletedBook = createBook();
    deletedBook.softDelete();
    deletedBook = bookRepository.save(deletedBook);

    bookRepository.flush();

    List<Book> result =
        bookRepository.findAllByIdInAndDeletedAtIsNull(
            List.of(
                activeBook.getId(),
                deletedBook.getId()
            )
        );

    assertThat(result)
        .extracting(Book::getId)
        .containsExactly(activeBook.getId());
  }

  @Test
  void findAllByCursor_sortsByRatingDesc() {
    Book bookA = bookRepository.save(new Book(
        "도서 A",
        "저자 A",
        "설명 A",
        "출판사 A",
        LocalDate.of(2026, 1, 1),
        "9781234567891"
    ));

    Book bookB = bookRepository.save(new Book(
        "도서 B",
        "저자 B",
        "설명 B",
        "출판사 B",
        LocalDate.of(2026, 1, 2),
        "9781234567892"
    ));

    User user1 = userRepository.save(
        User.create("user1@test.com", "user1", "password")
    );
    User user2 = userRepository.save(
        User.create("user2@test.com", "user2", "password")
    );
    User user3 = userRepository.save(
        User.create("user3@test.com", "user3", "password")
    );

    // A: 평균 4.0, 리뷰 2개
    reviewRepository.save(Review.create(user1, bookA, "좋아요", 5));
    reviewRepository.save(Review.create(user2, bookA, "괜찮아요", 3));

    // B: 평균 5.0, 리뷰 1개
    reviewRepository.save(Review.create(user3, bookB, "최고예요", 5));

    BookSearchRequest request = new BookSearchRequest(
        null,
        "rating",
        "DESC",
        null,
        null,
        10
    );

    List<BookSearchResult> results =
        bookRepository.findAllByCursor(request);

    assertThat(results).hasSize(2);

    assertThat(results.get(0).book().getTitle()).isEqualTo("도서 B");
    assertThat(results.get(0).reviewCount()).isEqualTo(1L);
    assertThat(results.get(0).rating()).isEqualTo(5.0);

    assertThat(results.get(1).book().getTitle()).isEqualTo("도서 A");
    assertThat(results.get(1).reviewCount()).isEqualTo(2L);
    assertThat(results.get(1).rating()).isEqualTo(4.0);
  }

  @Test
  void findAllByCursor_sortsByReviewCountDesc() {
    Book bookA = bookRepository.save(new Book(
        "도서 A",
        "저자 A",
        "설명 A",
        "출판사 A",
        LocalDate.of(2026, 1, 1),
        "9781234567891"
    ));

    Book bookB = bookRepository.save(new Book(
        "도서 B",
        "저자 B",
        "설명 B",
        "출판사 B",
        LocalDate.of(2026, 1, 2),
        "9781234567892"
    ));

    User user1 = userRepository.save(
        User.create("user1@test.com", "user1", "password")
    );
    User user2 = userRepository.save(
        User.create("user2@test.com", "user2", "password")
    );
    User user3 = userRepository.save(
        User.create("user3@test.com", "user3", "password")
    );

    // A: 평균 4.0, 리뷰 2개
    reviewRepository.save(Review.create(user1, bookA, "좋아요", 5));
    reviewRepository.save(Review.create(user2, bookA, "괜찮아요", 3));

    // B: 평균 5.0, 리뷰 1개
    reviewRepository.save(Review.create(user3, bookB, "최고예요", 5));

    BookSearchRequest request = new BookSearchRequest(
        null,
        "reviewCount",
        "DESC",
        null,
        null,
        10
    );

    List<BookSearchResult> results =
        bookRepository.findAllByCursor(request);

    assertThat(results).hasSize(2);

    // 리뷰 2개인 A가 먼저
    assertThat(results.get(0).book().getTitle()).isEqualTo("도서 A");
    assertThat(results.get(0).reviewCount()).isEqualTo(2L);
    assertThat(results.get(0).rating()).isEqualTo(4.0);

    // 리뷰 1개인 B가 다음
    assertThat(results.get(1).book().getTitle()).isEqualTo("도서 B");
    assertThat(results.get(1).reviewCount()).isEqualTo(1L);
    assertThat(results.get(1).rating()).isEqualTo(5.0);
  }

  @Test
  void findAllByCursor_returnsBooksAfterRatingCursor() {
    Book bookA = bookRepository.save(new Book(
        "도서 A",
        "저자 A",
        "설명 A",
        "출판사 A",
        LocalDate.of(2026, 1, 1),
        "9781234567891"
    ));

    Book bookB = bookRepository.save(new Book(
        "도서 B",
        "저자 B",
        "설명 B",
        "출판사 B",
        LocalDate.of(2026, 1, 2),
        "9781234567892"
    ));

    User user1 = userRepository.save(
        User.create("user1@test.com", "user1", "password")
    );
    User user2 = userRepository.save(
        User.create("user2@test.com", "user2", "password")
    );
    User user3 = userRepository.save(
        User.create("user3@test.com", "user3", "password")
    );

    // A: 평균 4.0
    reviewRepository.save(Review.create(user1, bookA, "좋아요", 5));
    reviewRepository.save(Review.create(user2, bookA, "괜찮아요", 3));

    // B: 평균 5.0
    reviewRepository.save(Review.create(user3, bookB, "최고예요", 5));

    BookSearchRequest request = new BookSearchRequest(
        null,
        "rating",
        "DESC",
        "5.0",
        null,
        10
    );

    List<BookSearchResult> results =
        bookRepository.findAllByCursor(request);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).book().getTitle()).isEqualTo("도서 A");
    assertThat(results.get(0).rating()).isEqualTo(4.0);
  }

  private Book createBook() {
    return new Book(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 19),
        UUID.randomUUID().toString()
    );
  }
}