package com.deokhugam.book.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class BookInfoNotFoundException extends DeokhugamException {

  public BookInfoNotFoundException(String isbn) {
    super(ErrorCode.BOOK_NOT_FOUND, Map.of("isbn", isbn));
  }
}