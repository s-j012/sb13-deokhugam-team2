package com.deokhugam.dashboard.controller;

import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.dashboard.dto.response.PopularBookDto;
import com.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.deokhugam.dashboard.dto.response.PowerUserDto;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.service.DashboardQueryService;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class DashboardController {

  private final DashboardQueryService dashboardQueryService;

  @GetMapping("/books/popular")
  public ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
      @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
      @RequestParam(name = "direction", defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(name = "limit", defaultValue = "50") @Positive int limit
  ) {
    return ResponseEntity.ok(
        dashboardQueryService.getPopularBooks(period, direction, cursor, after, limit)
    );
  }

  @GetMapping("/reviews/popular")
  public ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
      @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
      @RequestParam(name = "direction", defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(name = "limit", defaultValue = "50") @Positive int limit
  ) {
    return ResponseEntity.ok(
        dashboardQueryService.getPopularReviews(period, direction, cursor, after, limit)
    );
  }

  @GetMapping("/users/power")
  public ResponseEntity<CursorPageResponse<PowerUserDto>> getPowerUsers(
      @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
      @RequestParam(name = "direction", defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(name = "limit", defaultValue = "50") @Positive int limit
  ) {
    return ResponseEntity.ok(
        dashboardQueryService.getPowerUsers(period, direction, cursor, after, limit)
    );
  }
}
