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
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
  private final OcrSpaceClient ocrSpaceClient;

  private final ReviewRepository reviewRepository;
  private final ReviewLikeRepository reviewLikeRepository;
  private final CommentRepository commentRepository;
  private final NotificationRepository notificationRepository;
  private final ReviewRankingRepository reviewRankingRepository;
  private final BookRankingRepository bookRankingRepository;

  private static final Pattern ISBN_13_PATTERN =
      Pattern.compile("(?:978|979)(?:[-\\s]?\\d){10}");

  @Override
  @Transactional
  public BookDto create(BookCreateRequest request, MultipartFile thumbnailImage) {

    if (request.isbn() != null
        && !request.isbn().isBlank()
        && bookRepository.existsByIsbn(request.isbn())) {
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

      nextCursor = createNextCursor(request, lastResult);

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
  @Transactional
  public void hardDelete(UUID bookId) {

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    List<Review> reviews = reviewRepository.findAllByBookId(bookId);

    for (Review review : reviews) {
      UUID reviewId = review.getId();

      reviewLikeRepository.deleteAllByReviewId(reviewId);
      notificationRepository.deleteAllByReviewId(reviewId);
      commentRepository.deleteAllByReviewId(reviewId);
      reviewRankingRepository.deleteAllByReviewId(reviewId);
    }

    reviewRepository.deleteAll(reviews);

    bookRankingRepository.deleteAllByBookId(bookId);

    String thumbnailPath = book.getThumbnailUrl();

    if (thumbnailPath != null && !thumbnailPath.isBlank()) {
      storage.delete(thumbnailPath);
    }

    bookRepository.delete(book);
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

    String thumbnailImage = kakaoBookClient.findThumbnailBase64(document.thumbnail());

    if (thumbnailImage == null || thumbnailImage.isBlank()) {
      thumbnailImage = googleBookClient.findThumbnailBase64ByIsbn(isbn);
    }

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

  @Override
  public String extractIsbnFromImage(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new IsbnOcrFailedException();
    }

    OcrSpaceResponse response = ocrSpaceClient.parseImage(image);
    validateOcrResponse(response);

    String parsedText = extractParsedText(response);
    Matcher matcher = ISBN_13_PATTERN.matcher(parsedText);

    if (!matcher.find()) {
      throw new IsbnOcrFailedException();
    }

    return matcher.group()
        .replaceAll("[^0-9]", "");
  }

  private void validateOcrResponse(OcrSpaceResponse response) {
    if (response == null
        || response.erroredOnProcessing()
        || response.parsedResults() == null
        || response.parsedResults().isEmpty()) {
      throw new IsbnOcrFailedException();
    }
  }

  private String extractParsedText(OcrSpaceResponse response) {
    return response.parsedResults().stream()
        .map(OcrSpaceResponse.ParsedResult::parsedText)
        .filter(text -> text != null && !text.isBlank())
        .collect(Collectors.joining(" "));
  }

  private String createNextCursor(
      BookSearchRequest request,
      BookSearchResult result
  ) {
    Book book = result.book();

    return switch (request.orderBy()) {
      case "publishedDate" -> book.getPublishedDate().toString();
      case "title" -> book.getTitle();
      case "rating" -> String.valueOf(result.rating());
      case "reviewCount" -> String.valueOf(result.reviewCount());
      default -> book.getTitle();
    };
  }
}
