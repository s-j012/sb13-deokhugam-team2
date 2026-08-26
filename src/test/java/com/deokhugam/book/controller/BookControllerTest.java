package com.deokhugam.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.request.BookUpdateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.BookInfoResponse;
import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @Mock
  private BookService bookService;

  @BeforeEach
  void setUp() {
    BookController bookController = new BookController(bookService);

    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders
        .standaloneSetup(bookController)
        .setValidator(validator)
        .build();
  }

  @Test
  @DisplayName("도서를 등록하면 201 Created를 반환한다")
  void createBook() throws Exception {
    UUID bookId = UUID.randomUUID();

    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        "9781234567890"
    );

    BookDto response = new BookDto(
        bookId,
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        "9781234567890",
        "https://example.com/thumbnail.jpg",
        0,
        0.0,
        LocalDateTime.of(2026, 8, 25, 9, 0),
        LocalDateTime.of(2026, 8, 25, 9, 0)
    );

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile thumbnailImage = new MockMultipartFile(
        "thumbnailImage",
        "thumbnail.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test-image".getBytes()
    );

    given(bookService.create(
        any(BookCreateRequest.class),
        any(MultipartFile.class)
    )).willReturn(response);

    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
                .file(thumbnailImage)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("테스트 도서"))
        .andExpect(jsonPath("$.isbn").value("9781234567890"));

    then(bookService)
        .should()
        .create(
            any(BookCreateRequest.class),
            any(MultipartFile.class)
        );
  }

  @Test
  @DisplayName("도서 등록 시 ISBN 형식이 올바르지 않으면 400 Bad Request를 반환한다")
  void createBookWithInvalidIsbn() throws Exception {
    BookCreateRequest request = new BookCreateRequest(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        "1234"
    );

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
        )
        .andExpect(status().isBadRequest());

    then(bookService)
        .shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("도서 등록 시 필수값이 비어 있으면 400 Bad Request를 반환한다")
  void createBookWithBlankTitle() throws Exception {
    BookCreateRequest request = new BookCreateRequest(
        "",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        "9781234567890"
    );

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
        )
        .andExpect(status().isBadRequest());

    then(bookService)
        .shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("도서 이미지에서 ISBN을 추출하면 200 OK를 반환한다")
  void extractIsbnFromImage() throws Exception {
    String isbn = "9791139721973";

    MockMultipartFile image = new MockMultipartFile(
        "image",
        "book.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test-image".getBytes()
    );

    given(bookService.extractIsbnFromImage(any(MultipartFile.class)))
        .willReturn(isbn);

    mockMvc.perform(
            multipart("/api/books/isbn/ocr")
                .file(image)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(isbn));

    then(bookService)
        .should()
        .extractIsbnFromImage(any(MultipartFile.class));
  }

  @Test
  @DisplayName("도서 정보를 수정하면 200 OK를 반환한다")
  void updateBook() throws Exception {
    UUID bookId = UUID.randomUUID();

    BookUpdateRequest request = new BookUpdateRequest(
        "수정된 제목",
        "수정된 저자",
        "수정된 설명",
        "수정된 출판사",
        LocalDate.of(2026, 8, 25)
    );

    BookDto response = new BookDto(
        bookId,
        "수정된 제목",
        "수정된 저자",
        "수정된 설명",
        "수정된 출판사",
        LocalDate.of(2026, 8, 25),
        "9781234567890",
        "https://example.com/thumbnail.jpg",
        3,
        4.5,
        LocalDateTime.of(2026, 8, 25, 9, 0),
        LocalDateTime.of(2026, 8, 25, 10, 0)
    );

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile thumbnailImage = new MockMultipartFile(
        "thumbnailImage",
        "thumbnail.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "test-image".getBytes()
    );

    given(bookService.update(
        eq(bookId),
        any(BookUpdateRequest.class),
        any(MultipartFile.class)
    )).willReturn(response);

    mockMvc.perform(
            multipart(HttpMethod.PATCH, "/api/books/{bookId}", bookId)
                .file(bookData)
                .file(thumbnailImage)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("수정된 제목"))
        .andExpect(jsonPath("$.author").value("수정된 저자"))
        .andExpect(jsonPath("$.isbn").value("9781234567890"));

    then(bookService)
        .should()
        .update(
            eq(bookId),
            any(BookUpdateRequest.class),
            any(MultipartFile.class)
        );
  }

  @Test
  @DisplayName("ISBN으로 도서 정보를 조회하면 200 OK를 반환한다")
  void findBookInfoByIsbn() throws Exception {
    String isbn = "9781234567890";

    BookInfoResponse response = new BookInfoResponse(
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        isbn,
        "base64-thumbnail"
    );

    given(bookService.findBookInfoByIsbn(isbn))
        .willReturn(response);

    mockMvc.perform(
            get("/api/books/info")
                .param("isbn", isbn)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("테스트 도서"))
        .andExpect(jsonPath("$.author").value("테스트 저자"))
        .andExpect(jsonPath("$.isbn").value(isbn))
        .andExpect(jsonPath("$.thumbnailImage").value("base64-thumbnail"));

    then(bookService)
        .should()
        .findBookInfoByIsbn(isbn);
  }

  @Test
  @DisplayName("도서 목록을 조회하면 200 OK와 커서 페이지 응답을 반환한다")
  void findAllBooks() throws Exception {
    UUID bookId = UUID.randomUUID();

    BookDto book = new BookDto(
        bookId,
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        "9781234567890",
        "https://example.com/thumbnail.jpg",
        3,
        4.5,
        LocalDateTime.of(2026, 8, 25, 9, 0),
        LocalDateTime.of(2026, 8, 25, 9, 0)
    );

    CursorPageResponse<BookDto> response =
        new CursorPageResponse<>(
            List.of(book),
            null,
            null,
            1,
            1L,
            false
        );

    given(bookService.findAll(any(BookSearchRequest.class)))
        .willReturn(response);

    mockMvc.perform(
            get("/api/books")
                .param("keyword", "테스트")
                .param("orderBy", "title")
                .param("direction", "DESC")
                .param("limit", "10")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(bookId.toString()))
        .andExpect(jsonPath("$.content[0].title").value("테스트 도서"))
        .andExpect(jsonPath("$.size").value(1))
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.hasNext").value(false));

    then(bookService)
        .should()
        .findAll(any(BookSearchRequest.class));
  }

  @Test
  @DisplayName("도서 상세 정보를 조회하면 200 OK를 반환한다")
  void findBookById() throws Exception {
    UUID bookId = UUID.randomUUID();

    BookDto response = new BookDto(
        bookId,
        "테스트 도서",
        "테스트 저자",
        "테스트 설명",
        "테스트 출판사",
        LocalDate.of(2026, 8, 25),
        "9781234567890",
        "https://example.com/thumbnail.jpg",
        3,
        4.5,
        LocalDateTime.of(2026, 8, 25, 9, 0),
        LocalDateTime.of(2026, 8, 25, 9, 0)
    );

    given(bookService.findById(bookId))
        .willReturn(response);

    mockMvc.perform(
            get("/api/books/{bookId}", bookId)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(bookId.toString()))
        .andExpect(jsonPath("$.title").value("테스트 도서"))
        .andExpect(jsonPath("$.author").value("테스트 저자"))
        .andExpect(jsonPath("$.reviewCount").value(3))
        .andExpect(jsonPath("$.rating").value(4.5));

    then(bookService)
        .should()
        .findById(bookId);
  }

  @Test
  @DisplayName("도서를 논리 삭제하면 204 No Content를 반환한다")
  void deleteBook() throws Exception {
    UUID bookId = UUID.randomUUID();

    mockMvc.perform(
            delete("/api/books/{bookId}", bookId)
        )
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    then(bookService)
        .should()
        .delete(bookId);
  }

  @Test
  @DisplayName("도서를 물리 삭제하면 204 No Content를 반환한다")
  void hardDeleteBook() throws Exception {
    UUID bookId = UUID.randomUUID();

    mockMvc.perform(
            delete("/api/books/{bookId}/hard", bookId)
        )
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    then(bookService)
        .should()
        .hardDelete(bookId);
  }
}