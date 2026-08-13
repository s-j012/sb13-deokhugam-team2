package com.deokhugam.book.service;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.entity.Book;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

  BookDto create(BookCreateRequest request, MultipartFile thumbnailImage);
  BookDto findById(UUID bookId);
  CursorPageResponse<BookDto> findAll(BookSearchRequest request);
  void update();
  void delete();


}
