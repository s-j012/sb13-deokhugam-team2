package com.deokhugam.book.service;

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
import com.deokhugam.book.external.google.GoogleBookClient;
import com.deokhugam.book.external.kakao.KakaoBookClient;
import com.deokhugam.book.external.kakao.KakaoBookSearchResponse;
import com.deokhugam.book.mapper.BookMapper;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.global.storage.Storage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicBookService implements BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final Storage storage;
  private final KakaoBookClient kakaoBookClient;
  private final GoogleBookClient googleBookClient;

  @Override
  @Transactional
  public BookDto create(BookCreateRequest request, MultipartFile thumbnailImage) {

    if (bookRepository.existsByIsbn(request.isbn())) {
      throw new DuplicateBookException(request.isbn());
    }

    Book book = bookMapper.toEntity(request);

    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      String thumbnailPath = storage.upload(thumbnailImage);
      book.updateThumbnailUrl(thumbnailPath);
    }

    Book savedBook = bookRepository.save(book);

    return toDto(savedBook);
  }

  @Override
  public BookDto findById(UUID bookId) {

    BookSearchResult result = bookRepository.findByIdWithReviewStats(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    return toDto(result.book(), result.reviewCount(), result.rating());
  }

  @Override
  public CursorPageResponse<BookDto> findAll(BookSearchRequest request) {

    List<BookSearchResult> results = bookRepository.findAllByCursor(request);

    boolean hasNext = results.size() > request.limit();

    List<BookSearchResult> pageResults = hasNext ? results.subList(0, request.limit()) : results;

    List<BookDto> content = pageResults.stream()
        .map(result -> toDto(
            result.book(),
            result.reviewCount(),
            result.rating()
        ))
        .toList();

    String nextCursor = null;
    LocalDateTime nextAfter = null;

    if (hasNext && !pageResults.isEmpty()) {
      BookSearchResult lastResult = pageResults.get(pageResults.size() - 1);
      Book lastBook = lastResult.book();

      nextCursor = switch (request.orderBy()) {
        case "publishedDate" -> lastBook.getPublishedDate().toString();
        case "title" -> lastBook.getTitle();
        case "rating" -> String.valueOf(lastResult.rating());
        case "reviewCount" -> String.valueOf(lastResult.reviewCount());
        default -> lastBook.getTitle();
      };

      nextAfter = lastBook.getCreatedAt();
    }

    long totalElements = bookRepository.countAll(request);

    return new CursorPageResponse<>(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  @Override
  @Transactional
  public BookDto update(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) {

    BookSearchResult result = bookRepository.findByIdWithReviewStats(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    Book book = result.book();

    book.update(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate()
    );

    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      String oldThumbnailPath = book.getThumbnailUrl();
      String newThumbnailPath = storage.upload(thumbnailImage);

      book.updateThumbnailUrl(newThumbnailPath);

      if (oldThumbnailPath != null && !oldThumbnailPath.isBlank()) {
        storage.delete(oldThumbnailPath);
      }
    }

    return toDto(book, result.reviewCount(), result.rating());
  }

  @Override
  @Transactional
  public void delete(UUID bookId) {

    Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    book.softDelete();
  }

  private BookDto toDto(Book book) {
    return toDto(book, 0L, 0.0);
  }

  private BookDto toDto(Book book, long reviewCount, double rating) {
    String thumbnailUrl = book.getThumbnailUrl();

    if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
      thumbnailUrl = storage.getUrl(thumbnailUrl);
    }

    return bookMapper.toDto(
        book,
        thumbnailUrl,
        Math.toIntExact(reviewCount),
        rating
    );
  }

  @Override
  public BookInfoResponse findBookInfoByIsbn(String isbn) {
    KakaoBookSearchResponse response = kakaoBookClient.searchByIsbn(isbn);

    if (response == null || response.documents() == null || response.documents().isEmpty()) {
      throw new BookInfoNotFoundException(isbn);
    }

    KakaoBookSearchResponse.Document document = response.documents().get(0);

    String author = String.join(", ", document.authors());

    LocalDate publishedDate =
        document.datetime() != null ? document.datetime().toLocalDate() : null;

    String thumbnailImage =
        googleBookClient.findThumbnailBase64ByIsbn(isbn);

    return new BookInfoResponse(
        document.title(),
        author,
        document.contents(),
        document.publisher(),
        publishedDate,
        isbn,
        thumbnailImage
    );
  }
}
