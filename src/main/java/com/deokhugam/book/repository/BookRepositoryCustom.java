package com.deokhugam.book.repository;

import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookSearchResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepositoryCustom {

  List<BookSearchResult> findAllByCursor(BookSearchRequest request);

  long countAll(BookSearchRequest request);

  Optional<BookSearchResult> findByIdWithReviewStats(UUID bookId);
}
