package com.deokhugam.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record BookUpdateRequest(
    @Size(max = 150)
    @NotBlank(message = "도서 제목은 필수입니다.")
    String title,

    @Size(max = 50)
    @NotBlank(message = "저자는 필수입니다.")
    String author,

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
    String description,

    @Size(max = 50)
    @NotBlank(message = "출판사는 필수입니다.")
    String publisher,

    @NotNull(message = "출판일은 필수입니다.")
    LocalDate publishedDate
) {

}
