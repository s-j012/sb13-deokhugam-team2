package com.deokhugam.book.repository;

import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookSearchResult;
import com.deokhugam.book.entity.Book;
import com.deokhugam.book.enums.BookSortField;
import com.deokhugam.book.enums.SortDirection;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

  private final EntityManager entityManager;

  @Override
  public List<BookSearchResult> findAllByCursor(BookSearchRequest request) {

    StringBuilder jpql = new StringBuilder(
        """
            SELECT b, COUNT(r.id), COALESCE(AVG(r.rating), 0.0)
            FROM Book b
            LEFT JOIN Review r
                ON r.book = b
                AND r.deletedAt IS NULL
            WHERE b.deletedAt IS NULL
            """
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
      case RATING -> "COALESCE(AVG(r.rating), 0.0)";
      case REVIEW_COUNT -> "COUNT(r.id)";
    };

    boolean aggregateSort =
        sortField == BookSortField.RATING
            || sortField == BookSortField.REVIEW_COUNT;

    String direction = sortDirection.name();

    boolean hasCursor = request.cursor() != null && !request.cursor().isBlank();

    String operator = sortDirection == SortDirection.DESC ? "<" : ">";

    if (hasCursor && !aggregateSort) {
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

    jpql.append(" GROUP BY b");

    if (hasCursor && aggregateSort) {
      jpql.append(" HAVING (")
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

    var query = entityManager.createQuery(jpql.toString(), Object[].class);

    if (hasKeyword) {
      query.setParameter("keyword", "%" + request.keyword() + "%");
    }

    if (hasCursor) {
      Object cursorValue = switch (sortField) {
        case PUBLISHED_DATE -> LocalDate.parse(request.cursor());
        case TITLE -> request.cursor();
        case RATING -> Double.parseDouble(request.cursor());
        case REVIEW_COUNT -> Long.parseLong(request.cursor());
      };

      query.setParameter("cursor", cursorValue);

      if (request.after() != null) {
        query.setParameter("after", request.after());
      }
    }

    query.setMaxResults(request.limit() + 1);

    List<Object[]> results = query.getResultList();

    return results.stream()
        .map(result -> new BookSearchResult(
            (Book) result[0],
            ((Number) result[1]).longValue(),
            ((Number) result[2]).doubleValue()
        ))
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

  @Override
  public Optional<BookSearchResult> findByIdWithReviewStats(UUID bookId) {

    String jpql = """      
          SELECT b, COUNT(r.id), COALESCE(AVG(r.rating), 0.0)
        FROM Book b
        LEFT JOIN Review r
            ON r.book = b
            AND r.deletedAt IS NULL
        WHERE b.id = :bookId
          AND b.deletedAt IS NULL
        GROUP BY b
        """;

    List<Object[]> results = entityManager
        .createQuery(jpql, Object[].class)
        .setParameter("bookId", bookId)
        .getResultList();

    if (results.isEmpty()) {
      return Optional.empty();
    }

    Object[] result = results.get(0);

    return Optional.of(new BookSearchResult(
        (Book) result[0],
        ((Number) result[1]).longValue(),
        ((Number) result[2]).doubleValue()
    ));
  }
}
