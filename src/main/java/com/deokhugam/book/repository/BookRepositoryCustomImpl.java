package com.deokhugam.book.repository;

import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.entity.Book;
import com.deokhugam.book.enums.BookSortField;
import com.deokhugam.book.enums.SortDirection;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
      jpql.append(" AND (")
          .append("LOWER(b.title) LIKE LOWER(:keyword)")
          .append(" OR LOWER(b.author) LIKE LOWER(:keyword)")
          .append(" OR b.isbn LIKE :keyword")
          .append(")");
    }

    BookSortField sortField = BookSortField.from(request.orderBy());
    SortDirection sortDirection = SortDirection.from(request.direction());

    String sortFieldPath = switch (sortField) {
      case PUBLISHED_DATE -> "b.publishedDate";
      case TITLE -> "b.title";
      default -> "b.title";
    };

    String direction = sortDirection.name();

    boolean hasCursor = request.cursor() != null && !request.cursor().isBlank();

    String operator = sortDirection == SortDirection.DESC ? "<" : ">";

    if (hasCursor) {
      jpql.append(" AND (")
          .append(sortFieldPath)
          .append(" ")
          .append(operator)
          .append(" :cursor");

      if (request.after() != null) {
        jpql.append(" OR (")
            .append(sortFieldPath)
            .append(" = :cursor AND b.createdAt ")
            .append(operator)
            .append(" :after)");
      }

      jpql.append(")");
    }

    jpql.append(" ORDER BY ")
        .append(sortFieldPath)
        .append(" ")
        .append(direction)
        .append(", b.createdAt ")
        .append(direction);

    var query = entityManager.createQuery(jpql.toString(), Book.class);

    if (hasKeyword) {
      query.setParameter("keyword", "%" + request.keyword() + "%");
    }

    if (hasCursor) {
      Object cursorValue = switch (sortField) {
        case PUBLISHED_DATE -> LocalDate.parse(request.cursor());
        case TITLE -> request.cursor();
        default -> request.cursor();
      };

      query.setParameter("cursor", cursorValue);

      if (request.after() != null) {
        query.setParameter("after", request.after());
      }
    }

    query.setMaxResults(request.limit() + 1);

    List<Book> books = query.getResultList();

    return books.stream()
        .map(book -> new BookSearchResult(book, 0L, 0.0))
        .toList();
  }

  @Override
  public long countAll(BookSearchRequest request) {

    StringBuilder jpql = new StringBuilder(
        "SELECT COUNT(b) FROM Book b WHERE b.deletedAt IS NULL"
    );

    boolean hasKeyword = request.keyword() != null && !request.keyword().isBlank();

    if (hasKeyword) {
      jpql.append(" AND (")
          .append("LOWER(b.title) LIKE LOWER(:keyword)")
          .append(" OR LOWER(b.author) LIKE LOWER(:keyword)")
          .append(" OR b.isbn LIKE :keyword")
          .append(")");
    }

    var query = entityManager.createQuery(jpql.toString(), Long.class);

    if (hasKeyword) {
      query.setParameter(
          "keyword",
          "%" + request.keyword() + "%"
      );
    }

    return query.getSingleResult();
  }
}
