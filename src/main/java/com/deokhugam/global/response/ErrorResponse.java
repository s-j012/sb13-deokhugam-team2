package com.deokhugam.global.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    Map<String, Object> details
) {

  public static ErrorResponse of(
      int status,
      String message,
      Map<String, Object> details
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        status,
        message,
        details
    );
  }

  public static ErrorResponse of(
      int status,
      String message
  ) {
    return new ErrorResponse(
        LocalDateTime.now(),
        status,
        message,
        Map.of()
    );
  }
}