package com.deokhugam.book.controller.doc;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.request.BookUpdateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.BookInfoResponse;
import com.deokhugam.book.dto.response.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookControllerDoc {

  @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "도서 등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)"),
      @ApiResponse(responseCode = "409", description = "ISBN 중복"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<BookDto> create(
      @RequestPart("bookData") @Valid BookCreateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  );

  @Operation(summary = "OCR 기반 ISBN 인식", description = "OCR을 통해 ISBN을 인식합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "ISBN 인식 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 이미지 형식 또는 OCR 인식 실패"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<String> extractIsbn(
      @RequestPart("image") MultipartFile image
  );

  @Operation(summary = "도서 상세 정보 조회", description = "도서 ID로 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<BookDto> findById(
      @PathVariable UUID bookId
  );

  @Operation(summary = "도서 목록 조회", description = "검색 조건에 맞는 도서 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<CursorPageResponse<BookDto>> findAll(
      @Valid @ModelAttribute BookSearchRequest request
  );

  @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "409", description = "ISBN 중복"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<BookDto> update(
      @PathVariable UUID bookId,
      @RequestPart("bookData") @Valid BookUpdateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  );

  @Operation(summary = "도서 논리 삭제", description = "도서를 논리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> delete(
      @PathVariable UUID bookId
  );

  @Operation(summary = "도서 물리 삭제", description = "도서를 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> hardDelete(
      @PathVariable UUID bookId
  );

  @Operation(summary = "ISBN으로 도서 정보 조회", description = "외부 도서 API를 통해 ISBN으로 도서 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 ISBN 형식"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<BookInfoResponse> findBookInfoByIsbn(
      @RequestParam String isbn
  );
}