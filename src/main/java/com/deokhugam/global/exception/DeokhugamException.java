package com.deokhugam.global.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class DeokhugamException extends RuntimeException {

  private final ErrorCode errorCode;
  private final Map<String, Object> details;

  public DeokhugamException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = Map.of();
  }

  public DeokhugamException(
      ErrorCode errorCode,
      Map<String, Object> details
  ) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.details = details;
  }
}