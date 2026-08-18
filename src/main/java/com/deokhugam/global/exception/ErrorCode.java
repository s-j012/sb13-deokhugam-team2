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

  COMMENT_NOT_FOUND(
          HttpStatus.NOT_FOUND,
          "댓글을 찾을 수 없습니다."
  ),

  COMMENT_ACCESS_DENIED(
          HttpStatus.FORBIDDEN,
          "본인이 작성한 댓글만 수정하거나 삭제할 수 있습니다."
  ),

  COMMENT_ALREADY_DELETED(
          HttpStatus.BAD_REQUEST,
          "삭제된 댓글은 수정할 수 없습니다."
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