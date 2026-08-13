package com.deokhugam.book.repository;

import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.entity.Book;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

  private final EntityManager entityManager;

  @Override
  public List<BookSearchResult> findAllByCursor(BookSearchRequest request) {

    StringBuilder jpql = new StringBuilder(
        "SELECT b FROM Book b WHERE b.deletedAt IS NULL"
    );

    boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

    if (hasKeyword) {
      jpql.append("""
          AND (
            LOWER(b.title) LIKE LOWER(:keyword)
            OR LOWER(b.author) LIKE LOWER(:keyword)
            OR b.isbn LIKE :keyword
          )
          """);
    }

    String sortField = switch (request.orderBy()) {
      case "publishedDate" -> "b.publishedDate";
      case "title" -> "b.title";
      default -> "b.title";
    };

    String direction = "desc".equalsIgnoreCase(request.direction()) ? "DESC" : "ASC";

    jpql.append(" ORDER BY ").append(sortField).append(" ").append(direction);

    var query = entityManager.createQuery(jpql.toString(), Book.class);

    if (hasKeyword) {
      query.setParameter("keyword", "%" + request.keyword() + "%");
    }

    if (request.limit() > 0) {
      query.setMaxResults(request.limit() + 1);
    }

    List<Book> books = query.getResultList();

    return books.stream()
        .map(book -> new BookSearchResult(book, 0L, 0.0))
        .toList();
  }
}
