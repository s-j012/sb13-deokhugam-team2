package com.deokhugam.book.external.kakao;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoBookClient {

  private final RestClient restClient;
  private final RestClient imageRestClient;

  public KakaoBookClient(
      RestClient.Builder restClientBuilder,
      @Value("${deokhugam.kakao.rest-api-key}") String restApiKey
  ) {
    this.restClient = restClientBuilder
        .baseUrl("https://dapi.kakao.com")
        .defaultHeader("Authorization", "KakaoAK " + restApiKey)
        .build();
    this.imageRestClient = restClientBuilder.build();
  }

  public KakaoBookSearchResponse searchByIsbn(String isbn) {
    return restClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/v3/search/book")
            .queryParam("query", isbn)
            .queryParam("target", "isbn")
            .queryParam("size", 1)
            .build())
        .retrieve()
        .body(KakaoBookSearchResponse.class);
  }

  public String findThumbnailBase64(String thumbnailUrl) {
    if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
      return null;
    }

    try {
      URI thumbnailUri = URI.create(thumbnailUrl);
      String rawQuery = thumbnailUri.getRawQuery();

      if (rawQuery == null) {
        return null;
      }

      for (String parameter : rawQuery.split("&")) {
        String[] nameAndValue = parameter.split("=", 2);

        if (nameAndValue.length == 2 && nameAndValue[0].equals("fname")) {
          URI originalImageUri = URI.create(
              URLDecoder.decode(nameAndValue[1], StandardCharsets.UTF_8)
          );

          if (!"http".equals(originalImageUri.getScheme())
              && !"https".equals(originalImageUri.getScheme())) {
            return null;
          }

          byte[] imageBytes = imageRestClient.get()
              .uri(originalImageUri)
              .retrieve()
              .body(byte[].class);

          if (imageBytes == null || imageBytes.length == 0) {
            return null;
          }

          return Base64.getEncoder().encodeToString(imageBytes);
        }
      }

      return null;
    } catch (IllegalArgumentException | RestClientException e) {
      return null;
    }
  }
}
