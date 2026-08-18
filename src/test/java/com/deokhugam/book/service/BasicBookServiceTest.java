package com.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.request.BookUpdateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.entity.Book;
import com.deokhugam.book.exception.BookNotFoundException;
import com.deokhugam.book.exception.DuplicateBookException;
import com.deokhugam.book.mapper.BookMapper;
import com.deokhugam.book.repository.BookRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicBookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookMapper bookMapper;

  @InjectMocks
  private BasicBookService basicBookService;

  private BookCreateRequest request;

  @BeforeEach
  void setUp() {
    request = new BookCreateRequest(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330"
    );
  }

  @Test
  @DisplayName("도서를 정상적으로 등록한다.")
  void createBook() {

    Book book = new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn()
    );

    Book savedBook = book;

    BookDto expected = new BookDto(
        UUID.randomUUID(),
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn(),
        null,
        0,
        0.0,
        LocalDateTime.now(),
        LocalDateTime.now()
    );

    when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);
    when(bookMapper.toEntity(request)).thenReturn(book);
    when(bookRepository.save(book)).thenReturn(savedBook);
    when(bookMapper.toDto(savedBook)).thenReturn(expected);

    BookDto result = basicBookService.create(request, null);

    assertThat(result).isEqualTo(expected);

    verify(bookRepository).existsByIsbn(request.isbn());
    verify(bookRepository).save(book);
  }

  @Test
  @DisplayName("이미 등록된 ISBN이면 도서 등록에 실패한다.")
  void createBookDuplicateIsbn() {

    when(bookRepository.existsByIsbn(request.isbn())).thenReturn(true);

    assertThatThrownBy(() -> basicBookService.create(request, null))
        .isInstanceOf(DuplicateBookException.class);

    verify(bookRepository).existsByIsbn(request.isbn());
    verify(bookRepository, never()).save(any());
  }

  @Test
  @DisplayName("도서 ID로 상세 정보를 조회한다.")
  void findById() {

    UUID bookId = UUID.randomUUID();

    Book book = new Book(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330"
    );

    BookDto expected = new BookDto(
        bookId,
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330",
        null,
        0,
        0.0,
        LocalDateTime.now(),
        LocalDateTime.now()
    );

    when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
        .thenReturn(Optional.of(book));

    when(bookMapper.toDto(book))
        .thenReturn(expected);

    BookDto result = basicBookService.findById(bookId);

    assertThat(result).isEqualTo(expected);

    verify(bookRepository).findByIdAndDeletedAtIsNull(bookId);
  }

  @Test
  @DisplayName("존재하지 않는 도서를 조회하면 예외가 발생한다.")
  void findByIdNotFound() {

    UUID bookId = UUID.randomUUID();

    when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> basicBookService.findById(bookId))
        .isInstanceOf(BookNotFoundException.class);

    verify(bookRepository).findByIdAndDeletedAtIsNull(bookId);
    verify(bookMapper, never()).toDto(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("도서 정보를 정상적으로 수정한다.")
  void updateBook() {

    UUID bookId = UUID.randomUUID();

    Book book = new Book(
        "기존 제목",
        "기존 저자",
        "기존 설명",
        "기존 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330"
    );

    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목",
        "수정된 저자",
        "수정된 설명",
        "수정된 출판사",
        LocalDate.of(2025, 1, 1)
    );

    BookDto expected = new BookDto(
        bookId,
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        "9788960777330",
        null,
        0,
        0.0,
        LocalDateTime.now(),
        LocalDateTime.now()
    );

    when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
        .thenReturn(Optional.of(book));

    when(bookMapper.toDto(book))
        .thenReturn(expected);

    BookDto result = basicBookService.update(
        bookId,
        request,
        null
    );

    assertThat(result).isEqualTo(expected);

    assertThat(book.getTitle()).isEqualTo("수정된 제목");
    assertThat(book.getAuthor()).isEqualTo("수정된 저자");
    assertThat(book.getDescription()).isEqualTo("수정된 설명");
    assertThat(book.getPublisher()).isEqualTo("수정된 출판사");
    assertThat(book.getPublishedDate()).isEqualTo(LocalDate.of(2025, 1, 1));

    assertThat(book.getIsbn()).isEqualTo("9788960777330");

    verify(bookRepository).findByIdAndDeletedAtIsNull(bookId);
    verify(bookMapper).toDto(book);
  }

  @Test
  @DisplayName("도서를 정상적으로 논리 삭제한다.")
  void deleteBook() {

    UUID bookId = UUID.randomUUID();

    Book book = new Book(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330"
    );

    when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
        .thenReturn(Optional.of(book));

    basicBookService.delete(bookId);

    assertThat(book.getDeletedAt()).isNotNull();

    verify(bookRepository).findByIdAndDeletedAtIsNull(bookId);
  }

  @Test
  @DisplayName("존재하지 않는 도서를 삭제하면 예외가 발생한다.")
  void deleteBookNotFound() {

    UUID bookId = UUID.randomUUID();

    when(bookRepository.findByIdAndDeletedAtIsNull(bookId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> basicBookService.delete(bookId))
        .isInstanceOf(BookNotFoundException.class);

    verify(bookRepository).findByIdAndDeletedAtIsNull(bookId);
  }

  @Test
  @DisplayName("도서 목록을 커서 페이지네이션으로 조회한다.")
  void findAllBooks() {

    BookSearchRequest request = new BookSearchRequest(
        null,
        "title",
        "ASC",
        null,
        null,
        2
    );

    Book book1 = new Book(
        "가나다",
        "저자1",
        "설명1",
        "출판사1",
        LocalDate.of(2024, 1, 1),
        "9788960777331"
    );

    Book book2 = new Book(
        "라마바",
        "저자2",
        "설명2",
        "출판사2",
        LocalDate.of(2024, 1, 2),
        "9788960777332"
    );

    Book book3 = new Book(
        "사아자",
        "저자3",
        "설명3",
        "출판사3",
        LocalDate.of(2024, 1, 3),
        "9788960777333"
    );

    List<BookSearchResult> results = List.of(
        new BookSearchResult(book1, 0L, 0.0),
        new BookSearchResult(book2, 0L, 0.0),
        new BookSearchResult(book3, 0L, 0.0)
    );

    BookDto dto1 = new BookDto(
        UUID.randomUUID(),
        "가나다",
        "저자1",
        "설명1",
        "출판사1",
        LocalDate.of(2024, 1, 1),
        "9788960777331",
        null,
        0,
        0.0,
        null,
        null
    );

    BookDto dto2 = new BookDto(
        UUID.randomUUID(),
        "라마바",
        "저자2",
        "설명2",
        "출판사2",
        LocalDate.of(2024, 1, 2),
        "9788960777332",
        null,
        0,
        0.0,
        null,
        null
    );

    when(bookRepository.findAllByCursor(request))
        .thenReturn(results);

    when(bookMapper.toDto(book1))
        .thenReturn(dto1);

    when(bookMapper.toDto(book2))
        .thenReturn(dto2);

    when(bookRepository.countAll(request))
        .thenReturn(3L);

    CursorPageResponse<BookDto> response =
        basicBookService.findAll(request);

    assertThat(response.content()).hasSize(2);
    assertThat(response.hasNext()).isTrue();
    assertThat(response.totalElements()).isEqualTo(3);
    assertThat(response.nextCursor()).isEqualTo("라마바");

    verify(bookRepository).findAllByCursor(request);
    verify(bookRepository).countAll(request);
  }
}