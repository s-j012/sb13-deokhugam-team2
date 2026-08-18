package com.deokhugam.book.service;

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
import com.deokhugam.global.storage.Storage;
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

    Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    return toDto(book);
  }

  @Override
  public CursorPageResponse<BookDto> findAll(BookSearchRequest request) {

    List<BookSearchResult> results = bookRepository.findAllByCursor(request);

    boolean hasNext = results.size() > request.limit();

    List<BookSearchResult> pageResults = hasNext ? results.subList(0, request.limit()) : results;

    List<BookDto> content = pageResults.stream()
        .map(result -> toDto(result.book()))
        .toList();

    String nextCursor = null;
    LocalDateTime nextAfter = null;

    if (hasNext && !pageResults.isEmpty()) {
      Book lastBook = pageResults.get(pageResults.size() - 1).book();

      nextCursor = switch (request.orderBy()) {
        case "publishedDate" -> lastBook.getPublishedDate().toString();
        case "title" -> lastBook.getTitle();
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

    Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

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

    return toDto(book);
  }

  @Override
  @Transactional
  public void delete(UUID bookId) {

    Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId)
        .orElseThrow(() -> new BookNotFoundException(bookId));

    book.softDelete();
  }

  private BookDto toDto(Book book) {
    String thumbnailUrl = book.getThumbnailUrl();

    if (thumbnailUrl != null && !thumbnailUrl.isBlank()) {
      thumbnailUrl = storage.getUrl(thumbnailUrl);
    }

    return bookMapper.toDto(book, thumbnailUrl);
  }
}
