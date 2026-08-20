package com.deokhugam.book.external.kakao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoBookClient {

  private final RestClient restClient;

  public KakaoBookClient(
      RestClient.Builder restClientBuilder,
      @Value("${deokhugam.kakao.rest-api-key}") String restApiKey
  ) {
    this.restClient = restClientBuilder
        .baseUrl("https://dapi.kakao.com")
        .defaultHeader("Authorization", "KakaoAK " + restApiKey)
        .build();
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
}