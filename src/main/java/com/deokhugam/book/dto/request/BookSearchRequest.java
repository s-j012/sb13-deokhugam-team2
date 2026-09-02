package com.deokhugam.book.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public record BookSearchRequest(
    String keyword,

    @Pattern(
        regexp = "title|publishedDate|rating|reviewCount",
        message = "정렬 기준이 올바르지 않습니다."
    )
    String orderBy,

    @Pattern(
        regexp = "(?i)ASC|DESC",
        message = "정렬 방향은 ASC 또는 DESC여야 합니다."
    )
    String direction,

    String cursor,

    LocalDateTime after,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    Integer limit
) {

  public BookSearchRequest {
    if (orderBy == null || orderBy.isBlank()) {
      orderBy = "title";
    }

    if (direction == null || direction.isBlank()) {
      direction = "DESC";
    }

    if (limit == null) {
      limit = 50;
    }
  }
}
