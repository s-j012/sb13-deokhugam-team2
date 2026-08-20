package com.deokhugam.book.mapper;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

  default Book toEntity(BookCreateRequest request) {
    if (request == null) {
      return null;
    }

    return new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn()
    );
  }

  @Mapping(target = "thumbnailUrl", source = "thumbnailUrl")
  @Mapping(target = "reviewCount", source = "reviewCount")
  @Mapping(target = "rating", source = "rating")
  BookDto toDto(
      Book book,
      String thumbnailUrl,
      int reviewCount,
      double rating
  );
}
