package com.deokhugam.book.dto.request;

public record BookSearchRequest(
    String keyword,
    String orderBy,
    String direction,
    String cursor,
    String after,
    int limit
) {

}
