package com.deokhugam.book.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;

public class IsbnOcrFailedException extends DeokhugamException {

  public IsbnOcrFailedException() {
    super(ErrorCode.ISBN_OCR_FAILED, Map.of());
  }
}
