package com.deokhugam.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookUpdateRequest(
    @NotBlank(message = "도서 제목은 필수입니다.")
    String title,

    @NotBlank(message = "저자는 필수입니다.")
    String author,

    String description,

    @NotBlank(message = "출판사는 필수입니다.")
    String publisher,

    @NotNull(message = "출판일은 필수입니다.")
    LocalDate publishedDate
) {

}
