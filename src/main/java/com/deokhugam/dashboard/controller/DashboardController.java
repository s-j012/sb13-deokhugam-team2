package com.deokhugam.dashboard.controller;

import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.dashboard.dto.response.PopularBookDto;
import com.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.deokhugam.dashboard.dto.response.PowerUserDto;
import com.deokhugam.dashboard.entity.PeriodType;
import com.deokhugam.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/books/popular")
  public ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return ResponseEntity.ok(dashboardService.getPopularBooks(period, direction, cursor, after, limit));
  }

  @GetMapping("/reviews/popular")
  public ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return ResponseEntity.ok(dashboardService.getPopularReviews(period, direction, cursor, after, limit));
  }

  @GetMapping("/users/power")
  public ResponseEntity<CursorPageResponse<PowerUserDto>> getPowerUsers(
      @RequestParam(defaultValue = "DAILY") PeriodType period,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "50") int limit
  ) {
    return ResponseEntity.ok(dashboardService.getPowerUsers(period, direction, cursor, after, limit));
  }
}