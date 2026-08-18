package com.deokhugam.book.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookSortField {
  TITLE("title"),
  PUBLISHED_DATE("publishedDate"),
  RATING("rating"),
  REVIEW_COUNT("reviewCount");

  private final String value;

  public static BookSortField from(String value) {
    for (BookSortField field : values()) {
      if (field.value.equals(value)) {
        return field;
      }
    }

    throw new IllegalArgumentException("지원하지 않는 도서 정렬 기준입니다: " + value);
  }
}

