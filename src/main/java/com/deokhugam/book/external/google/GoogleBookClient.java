package com.deokhugam.book.external.google;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleBookClient {

  private final RestClient restClient;
  private final String apiKey;

  public GoogleBookClient(
      RestClient.Builder restClientBuilder,
      @Value("${deokhugam.google.books-api-key}") String apiKey
  ) {
    this.restClient = restClientBuilder
        .baseUrl("https://www.googleapis.com")
        .build();

    this.apiKey = apiKey;
  }

  public GoogleBookSearchResponse searchByIsbn(String isbn) {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/books/v1/volumes")
            .queryParam("q", "isbn:" + isbn)
            .queryParam("maxResults", 1)
            .queryParam("key", apiKey)
            .build())
        .retrieve()
        .body(GoogleBookSearchResponse.class);
  }

  public String findThumbnailByIsbn(String isbn) {
    GoogleBookSearchResponse response = searchByIsbn(isbn);

    if (response == null
        || response.items() == null
        || response.items().isEmpty()) {
      return null;
    }

    GoogleBookSearchResponse.VolumeInfo volumeInfo =
        response.items().get(0).volumeInfo();

    if (volumeInfo == null || volumeInfo.imageLinks() == null) {
      return null;
    }

    return volumeInfo.imageLinks().thumbnail();
  }

  public String findThumbnailBase64ByIsbn(String isbn) {
    try {
      String thumbnailUrl = findThumbnailByIsbn(isbn);

      if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
        return null;
      }

      String httpsUrl = thumbnailUrl.replace("http://", "https://");

      byte[] imageBytes = restClient.get()
          .uri(httpsUrl)
          .retrieve()
          .body(byte[].class);

      if (imageBytes == null || imageBytes.length == 0) {
        return null;
      }

      return Base64.getEncoder().encodeToString(imageBytes);

    } catch (RestClientException e) {
      return null;
    }
  }
}
