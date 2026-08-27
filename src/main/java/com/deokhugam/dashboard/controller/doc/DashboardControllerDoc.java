package com.deokhugam.dashboard.controller.doc;

import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.dashboard.dto.response.PopularBookDto;
import com.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.deokhugam.dashboard.entity.PeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "대시보드", description = "대시보드 관련 API")
public interface DashboardControllerDoc {

  @Operation(
      summary = "인기 도서 목록 조회",
      description = "기간별 인기 도서 랭킹을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  ResponseEntity<CursorPageResponse<PopularBookDto>> getPopularBooks(
      @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
      @RequestParam(name = "direction", defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(name = "limit", defaultValue = "50") @Positive int limit
  );

  @Operation(
      summary = "인기 리뷰 목록 조회",
      description = "기간별 인기 리뷰 랭킹을 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청")
  })
  ResponseEntity<CursorPageResponse<PopularReviewDto>> getPopularReviews(
      @RequestParam(name = "period", defaultValue = "DAILY") PeriodType period,
      @RequestParam(name = "direction", defaultValue = "ASC") Sort.Direction direction,
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "after", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
      @RequestParam(name = "limit", defaultValue = "50") @Positive int limit
  );
}