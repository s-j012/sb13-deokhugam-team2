package com.deokhugam.book.dto.response;

import java.time.LocalDate;

public record BookInfoResponse(
    String title,
    String author,
    String description,
    String publisher,
    LocalDate publishedDate,
    String isbn,
    String thumbnailImage
) {

}