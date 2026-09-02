package com.deokhugam.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.request.BookUpdateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.BookInfoResponse;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.entity.Book;
import com.deokhugam.book.exception.BookInfoNotFoundException;
import com.deokhugam.book.exception.BookNotFoundException;
import com.deokhugam.book.exception.DuplicateBookException;
import com.deokhugam.book.exception.IsbnOcrFailedException;
import com.deokhugam.book.external.google.GoogleBookClient;
import com.deokhugam.book.external.kakao.KakaoBookClient;
import com.deokhugam.book.external.kakao.KakaoBookSearchResponse;
import com.deokhugam.book.external.ocr.OcrSpaceClient;
import com.deokhugam.book.external.ocr.OcrSpaceResponse;
import com.deokhugam.book.mapper.BookMapper;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.dashboard.repository.BookRankingRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.global.storage.Storage;
import com.deokhugam.notification.repository.NotificationRepository;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewLikeRepository;
import com.deokhugam.review.repository.ReviewRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BasicBookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private BookMapper bookMapper;

  @Mock
  Storage storage;

  @InjectMocks
  private BasicBookService basicBookService;

  private BookCreateRequest request;

  @Mock
  KakaoBookClient kakaoBookClient;

  @Mock
  GoogleBookClient googleBookClient;

  @Mock
  OcrSpaceClient ocrSpaceClient;

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private ReviewLikeRepository reviewLikeRepository;

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private ReviewRankingRepository reviewRankingRepository;

  @Mock
  private BookRankingRepository bookRankingRepository;

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
    when(bookMapper.toDto(
        book,
        null,
        0,
        0.0
    )).thenReturn(expected);

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
  @DisplayName("썸네일과 함께 도서를 등록한다")
  void createBookWithThumbnail() {
    MultipartFile thumbnailImage = mock(MultipartFile.class);

    Book book = new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn()
    );

    BookDto expected = new BookDto(
        UUID.randomUUID(),
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn(),
        "https://example.com/thumbnail.jpg",
        0,
        0.0,
        null,
        null
    );

    when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);
    when(bookMapper.toEntity(request)).thenReturn(book);
    when(thumbnailImage.isEmpty()).thenReturn(false);
    when(storage.upload(thumbnailImage))
        .thenReturn("book-thumbnails/test.jpg");
    when(bookRepository.save(book)).thenReturn(book);
    when(storage.getUrl("book-thumbnails/test.jpg"))
        .thenReturn("https://example.com/thumbnail.jpg");
    when(bookMapper.toDto(
        book,
        "https://example.com/thumbnail.jpg",
        0,
        0.0
    )).thenReturn(expected);

    BookDto result = basicBookService.create(request, thumbnailImage);

    assertEquals(expected, result);
    assertEquals("book-thumbnails/test.jpg", book.getThumbnailUrl());

    verify(storage).upload(thumbnailImage);
    verify(storage).getUrl("book-thumbnails/test.jpg");
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

    BookSearchResult searchResult =
        new BookSearchResult(book, 2L, 4.5);

    when(bookRepository.findByIdWithReviewStats(bookId))
        .thenReturn(Optional.of(searchResult));

    when(bookMapper.toDto(
        book,
        null,
        2,
        4.5
    )).thenReturn(expected);

    BookDto result = basicBookService.findById(bookId);

    assertThat(result).isEqualTo(expected);

    verify(bookRepository).findByIdWithReviewStats(bookId);
  }

  @Test
  @DisplayName("존재하지 않는 도서를 조회하면 예외가 발생한다.")
  void findByIdNotFound() {

    UUID bookId = UUID.randomUUID();

    when(bookRepository.findByIdWithReviewStats(bookId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> basicBookService.findById(bookId))
        .isInstanceOf(BookNotFoundException.class);

    verify(bookRepository).findByIdWithReviewStats(bookId);
    verify(bookMapper, never()).toDto(
        any(Book.class),
        nullable(String.class),
        anyInt(),
        anyDouble()
    );
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
        3,
        4.0,
        LocalDateTime.now(),
        LocalDateTime.now()
    );

    BookSearchResult searchResult =
        new BookSearchResult(book, 3L, 4.0);

    when(bookRepository.findByIdWithReviewStats(bookId))
        .thenReturn(Optional.of(searchResult));

    when(bookMapper.toDto(
        book,
        null,
        3,
        4.0
    )).thenReturn(expected);

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

    verify(bookRepository).findByIdWithReviewStats(bookId);

    verify(bookMapper).toDto(
        book,
        null,
        3,
        4.0
    );
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

    when(bookMapper.toDto(
        book1,
        null,
        0,
        0.0
    )).thenReturn(dto1);

    when(bookMapper.toDto(
        book2,
        null,
        0,
        0.0
    )).thenReturn(dto2);

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

  @Test
  @DisplayName("ISBN으로 도서 정보를 조회하고 Kakao 원본 썸네일 Base64를 사용한다.")
  void findBookInfoByIsbn() {
    String isbn = "9788960777330";

    KakaoBookSearchResponse.Document document =
        new KakaoBookSearchResponse.Document(
            "자바 ORM 표준 JPA 프로그래밍",
            "JPA 학습용 도서",
            isbn,
            OffsetDateTime.parse("2015-07-28T00:00:00+09:00"),
            List.of("김영한"),
            "에이콘출판",
            "https://example.com/thumbnail.jpg"
        );

    KakaoBookSearchResponse response =
        new KakaoBookSearchResponse(List.of(document));

    when(kakaoBookClient.searchByIsbn(isbn))
        .thenReturn(response);
    when(kakaoBookClient.findThumbnailBase64("https://example.com/thumbnail.jpg"))
        .thenReturn("kakao-thumbnail-base64");

    BookInfoResponse result =
        basicBookService.findBookInfoByIsbn(isbn);

    assertEquals("자바 ORM 표준 JPA 프로그래밍", result.title());
    assertEquals("김영한", result.author());
    assertEquals(isbn, result.isbn());
    assertEquals(
        LocalDate.of(2015, 7, 28),
        result.publishedDate()
    );
    assertEquals("kakao-thumbnail-base64", result.thumbnailImage());
    verify(googleBookClient, never()).findThumbnailBase64ByIsbn(any());
  }

  @Test
  @DisplayName("ISBN으로 조회한 도서가 없으면 예외가 발생한다.")
  void findBookInfoByIsbnNotFound() {
    String isbn = "9780000000000";

    when(kakaoBookClient.searchByIsbn(isbn))
        .thenReturn(new KakaoBookSearchResponse(List.of()));

    assertThrows(
        BookInfoNotFoundException.class,
        () -> basicBookService.findBookInfoByIsbn(isbn)
    );
  }

  @Test
  @DisplayName("이미지에서 ISBN을 정상적으로 추출한다.")
  void extractIsbnFromImage_success() {
    MockMultipartFile image = new MockMultipartFile(
        "image",
        "book.jpg",
        "image/jpeg",
        "test-image".getBytes()
    );

    OcrSpaceResponse response = new OcrSpaceResponse(
        List.of(
            new OcrSpaceResponse.ParsedResult(
                "BOOK TITLE ISBN 978-89-374-6077-7"
            )
        ),
        false,
        null
    );

    given(ocrSpaceClient.parseImage(image))
        .willReturn(response);

    String isbn = basicBookService.extractIsbnFromImage(image);

    assertThat(isbn).isEqualTo("9788937460777");
  }

  @Test
  @DisplayName("OCR 결과에 ISBN이 없으면 예외가 발생한다.")
  void extractIsbnFromImage_throwsExceptionWhenIsbnNotFound() {
    MockMultipartFile image = new MockMultipartFile(
        "image",
        "book.jpg",
        "image/jpeg",
        "test-image".getBytes()
    );

    OcrSpaceResponse response = new OcrSpaceResponse(
        List.of(
            new OcrSpaceResponse.ParsedResult(
                "BOOK TITLE AUTHOR PUBLISHER"
            )
        ),
        false,
        null
    );

    given(ocrSpaceClient.parseImage(image))
        .willReturn(response);

    assertThatThrownBy(() ->
        basicBookService.extractIsbnFromImage(image)
    ).isInstanceOf(IsbnOcrFailedException.class);
  }

  @Test
  @DisplayName("OCR 처리에 실패하면 예외가 발생한다.")
  void extractIsbnFromImage_throwsExceptionWhenOcrFails() {
    MockMultipartFile image = new MockMultipartFile(
        "image",
        "book.jpg",
        "image/jpeg",
        "test-image".getBytes()
    );

    OcrSpaceResponse response = new OcrSpaceResponse(
        List.of(),
        true,
        "OCR processing failed"
    );

    given(ocrSpaceClient.parseImage(image))
        .willReturn(response);

    assertThatThrownBy(() ->
        basicBookService.extractIsbnFromImage(image)
    ).isInstanceOf(IsbnOcrFailedException.class);
  }

  @Test
  @DisplayName("빈 이미지가 전달되면 예외가 발생한다.")
  void extractIsbnFromImage_throwsExceptionWhenImageIsEmpty() {
    MockMultipartFile image = new MockMultipartFile(
        "image",
        "empty.jpg",
        "image/jpeg",
        new byte[0]
    );

    assertThatThrownBy(() ->
        basicBookService.extractIsbnFromImage(image)
    ).isInstanceOf(IsbnOcrFailedException.class);

    then(ocrSpaceClient).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("OCR 응답이 없으면 예외가 발생한다.")
  void extractIsbnFromImage_throwsExceptionWhenOcrResponseIsNull() {
    MockMultipartFile image = new MockMultipartFile(
        "image",
        "book.jpg",
        "image/jpeg",
        "image".getBytes()
    );

    given(ocrSpaceClient.parseImage(image))
        .willReturn(null);

    assertThatThrownBy(() ->
        basicBookService.extractIsbnFromImage(image)
    ).isInstanceOf(IsbnOcrFailedException.class);
  }

  @Test
  @DisplayName("도서를 물리 삭제하면 연관 데이터와 썸네일까지 함께 삭제한다")
  void hardDeleteBook() {
    // given
    UUID bookId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    Book book = mock(Book.class);
    Review review = mock(Review.class);

    given(bookRepository.findById(bookId))
        .willReturn(Optional.of(book));

    given(reviewRepository.findAllByBookId(bookId))
        .willReturn(List.of(review));

    given(review.getId())
        .willReturn(reviewId);

    given(book.getThumbnailUrl())
        .willReturn("books/test.jpg");

    // when
    basicBookService.hardDelete(bookId);

    // then
    then(reviewLikeRepository)
        .should()
        .deleteAllByReviewId(reviewId);

    then(notificationRepository)
        .should()
        .deleteAllByReviewId(reviewId);

    then(commentRepository)
        .should()
        .deleteAllByReviewId(reviewId);

    then(reviewRankingRepository)
        .should()
        .deleteAllByReviewId(reviewId);

    then(reviewRepository)
        .should()
        .deleteAll(List.of(review));

    then(bookRankingRepository)
        .should()
        .deleteAllByBookId(bookId);

    then(storage)
        .should()
        .delete("books/test.jpg");

    then(bookRepository)
        .should()
        .delete(book);
  }

  @Test
  @DisplayName("존재하지 않는 도서를 물리 삭제하면 예외가 발생한다")
  void hardDeleteBookNotFound() {
    // given
    UUID bookId = UUID.randomUUID();

    given(bookRepository.findById(bookId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicBookService.hardDelete(bookId))
        .isInstanceOf(BookNotFoundException.class);

    then(reviewRepository)
        .shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("Kakao 원본 썸네일 변환이 실패하면 Google Books 썸네일을 사용한다.")
  void findBookInfoByIsbnFallbackToGoogleThumbnail() {
    String isbn = "9788960777330";
    String googleThumbnailBase64 = "google-thumbnail-base64";

    KakaoBookSearchResponse.Document document =
        new KakaoBookSearchResponse.Document(
            "자바 ORM 표준 JPA 프로그래밍",
            "JPA 학습용 도서",
            isbn,
            OffsetDateTime.parse("2015-07-28T00:00:00+09:00"),
            List.of("김영한"),
            "에이콘출판",
            "https://example.com/kakao-thumbnail.jpg"
        );

    KakaoBookSearchResponse response =
        new KakaoBookSearchResponse(List.of(document));

    when(kakaoBookClient.searchByIsbn(isbn))
        .thenReturn(response);
    when(kakaoBookClient.findThumbnailBase64(
        "https://example.com/kakao-thumbnail.jpg"
    )).thenReturn(null);

    when(googleBookClient.findThumbnailBase64ByIsbn(isbn))
        .thenReturn(googleThumbnailBase64);

    BookInfoResponse result =
        basicBookService.findBookInfoByIsbn(isbn);

    assertEquals("자바 ORM 표준 JPA 프로그래밍", result.title());
    assertEquals("김영한", result.author());
    assertEquals(isbn, result.isbn());
    assertEquals(
        LocalDate.of(2015, 7, 28),
        result.publishedDate()
    );
    assertEquals(
        googleThumbnailBase64,
        result.thumbnailImage()
    );

    verify(googleBookClient)
        .findThumbnailBase64ByIsbn(isbn);
  }

  @Test
  @DisplayName("도서 썸네일 수정 트랜잭션 커밋 후 기존 썸네일을 삭제한다.")
  void updateBookThumbnailDeleteOldThumbnailAfterCommit() {

    UUID bookId = UUID.randomUUID();

    Book book = new Book(
        "기존 제목",
        "기존 저자",
        "기존 설명",
        "기존 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330"
    );
    book.updateThumbnailUrl("book-thumbnails/old.jpg");

    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목",
        "수정된 저자",
        "수정된 설명",
        "수정된 출판사",
        LocalDate.of(2025, 1, 1)
    );

    MultipartFile thumbnailImage = mock(MultipartFile.class);

    BookSearchResult searchResult =
        new BookSearchResult(book, 3L, 4.0);

    when(bookRepository.findByIdWithReviewStats(bookId))
        .thenReturn(Optional.of(searchResult));
    when(thumbnailImage.isEmpty()).thenReturn(false);
    when(storage.upload(thumbnailImage))
        .thenReturn("book-thumbnails/new.jpg");

    TransactionSynchronizationManager.initSynchronization();

    try {
      basicBookService.update(bookId, request, thumbnailImage);

      List<TransactionSynchronization> synchronizations =
          TransactionSynchronizationManager.getSynchronizations();

      assertThat(synchronizations).hasSize(1);

      // 커밋 전에는 기존 파일을 삭제하면 안 됨
      verify(storage, never()).delete("book-thumbnails/old.jpg");

      synchronizations.get(0).afterCommit();

      // 커밋 후 기존 파일 삭제
      verify(storage).delete("book-thumbnails/old.jpg");
      verify(storage, never()).delete("book-thumbnails/new.jpg");

    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  @Test
  @DisplayName("도서 썸네일 수정 트랜잭션 롤백 후 신규 썸네일을 삭제한다.")
  void updateBookThumbnailDeleteNewThumbnailAfterRollback() {

    UUID bookId = UUID.randomUUID();

    Book book = new Book(
        "기존 제목",
        "기존 저자",
        "기존 설명",
        "기존 출판사",
        LocalDate.of(2024, 1, 1),
        "9788960777330"
    );
    book.updateThumbnailUrl("book-thumbnails/old.jpg");

    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목",
        "수정된 저자",
        "수정된 설명",
        "수정된 출판사",
        LocalDate.of(2025, 1, 1)
    );

    MultipartFile thumbnailImage = mock(MultipartFile.class);

    BookSearchResult searchResult =
        new BookSearchResult(book, 3L, 4.0);

    when(bookRepository.findByIdWithReviewStats(bookId))
        .thenReturn(Optional.of(searchResult));
    when(thumbnailImage.isEmpty()).thenReturn(false);
    when(storage.upload(thumbnailImage))
        .thenReturn("book-thumbnails/new.jpg");

    TransactionSynchronizationManager.initSynchronization();

    try {
      basicBookService.update(bookId, request, thumbnailImage);

      List<TransactionSynchronization> synchronizations =
          TransactionSynchronizationManager.getSynchronizations();

      assertThat(synchronizations).hasSize(1);

      synchronizations.get(0).afterCompletion(
          TransactionSynchronization.STATUS_ROLLED_BACK
      );

      // 롤백하면 새 파일을 제거하고 기존 파일은 유지
      verify(storage).delete("book-thumbnails/new.jpg");
      verify(storage, never()).delete("book-thumbnails/old.jpg");

    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }
}
