package com.deokhugam.dashboard.controller;

import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.dashboard.dto.response.PopularBookDto;
import com.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.deokhugam.dashboard.dto.response.PowerUserDto;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.service.DashboardQueryService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class DashboardController {

  private final DashboardQueryService dashboardQueryService;

  @GetMapping("/books/popular")
  public ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(defaultValue = "50") @Positive int limit
  ) {
    return ResponseEntity.ok(dashboardQueryService.getPopularBooks(period, direction, cursor, after, limit));
  }

  @GetMapping("/reviews/popular")
  public ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(defaultValue = "50") @Positive int limit
  ) {
    return ResponseEntity.ok(dashboardQueryService.getPopularReviews(period, direction, cursor, after, limit));
  }

  @GetMapping("/users/power")
  public ResponseEntity<CursorPageResponse<PowerUserDto>> getPowerUsers(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(defaultValue = "50") @Positive int limit
  ) {
    return ResponseEntity.ok(dashboardQueryService.getPowerUsers(period, direction, cursor, after, limit));
  }
}