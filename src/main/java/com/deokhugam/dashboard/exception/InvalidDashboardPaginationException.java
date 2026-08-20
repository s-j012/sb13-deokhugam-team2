package com.deokhugam.dashboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDashboardPaginationException extends IllegalArgumentException {

  public InvalidDashboardPaginationException(String message) {
    super(message);
  }

  public InvalidDashboardPaginationException(String message, Throwable cause) {
    super(message, cause);
  }
}
