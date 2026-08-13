package com.deokhugam.global.exception;

import com.deokhugam.global.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DeokhugamException.class)
  public ResponseEntity<ErrorResponse> handleDeokhugamException(
      DeokhugamException e
  ) {
    ErrorCode errorCode = e.getErrorCode();

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(ErrorResponse.of(
            errorCode.getStatus().value(),
            errorCode.getMessage(),
            e.getDetails()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e
  ) {
    return ResponseEntity
        .badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "입력값이 올바르지 않습니다."
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "서버 내부 오류가 발생했습니다."
        ));
  }
}