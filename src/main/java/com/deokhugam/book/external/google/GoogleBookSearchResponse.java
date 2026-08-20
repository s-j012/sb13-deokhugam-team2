package com.deokhugam.book.external.google;

import java.util.List;

public record GoogleBookSearchResponse(List<Item> items) {

  public record Item(VolumeInfo volumeInfo) {}

  public record VolumeInfo(ImageLinks imageLinks) {}

  public record ImageLinks(String thumbnail) {}

}
