package com.deokhugam.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

  DUPLICATE_BOOK(
      HttpStatus.CONFLICT,
      "이미 등록된 ISBN입니다."
  ),

  BOOK_NOT_FOUND(
      HttpStatus.NOT_FOUND,
      "도서를 찾을 수 없습니다."
  ),

  INTERNAL_SERVER_ERROR(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "서버 내부 오류가 발생했습니다."
  );

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}