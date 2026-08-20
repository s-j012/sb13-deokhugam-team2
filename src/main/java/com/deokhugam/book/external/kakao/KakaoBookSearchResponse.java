package com.deokhugam.book.external.kakao;

import java.time.OffsetDateTime;
import java.util.List;

public record KakaoBookSearchResponse(
    List<Document> documents
) {

  public record Document(
      String title,
      String contents,
      String isbn,
      OffsetDateTime datetime,
      List<String> authors,
      String publisher,
      String thumbnail
  ) {
  }
}