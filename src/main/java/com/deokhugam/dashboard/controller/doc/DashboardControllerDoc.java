package com.deokhugam.dashboard.controller.doc;

import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.dashboard.dto.response.PopularBookDto;
import com.deokhugam.dashboard.dto.response.PopularReviewDto;
import com.deokhugam.dashboard.entity.PeriodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface DashboardControllerDoc {

  @Operation(
      summary = "인기 도서 목록 조회",
      description = "기간별 인기 도서 목록을 조회합니다.",
      tags = {"도서 관리"}
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "인기 도서 목록 조회 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)"
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
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
      description = "기간별 인기 리뷰 목록을 조회합니다.",
      tags = {"리뷰 관리"}
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "인기 리뷰 목록 조회 성공"),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)"
      ),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
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