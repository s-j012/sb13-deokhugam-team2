package com.deokhugam.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.global.config.JpaConfig;
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