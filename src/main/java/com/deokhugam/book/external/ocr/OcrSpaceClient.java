package com.deokhugam.book.external.ocr;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class OcrSpaceClient {

  private final RestClient restClient;
  private final String apiKey;

  public OcrSpaceClient(
      RestClient.Builder restClientBuilder,
      @Value("${deokhugam.ocr-space.api-key}") String apiKey
  ) {
    this.restClient = restClientBuilder
        .baseUrl("https://api.ocr.space")
        .build();

    this.apiKey = apiKey;
  }

  public OcrSpaceResponse parseImage(MultipartFile image) {
    try {
      ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
        @Override
        public String getFilename() {
          return image.getOriginalFilename();
        }
      };

      MultipartBodyBuilder builder = new MultipartBodyBuilder();

      String contentType = image.getContentType();

      MediaType mediaType = contentType != null
          ? MediaType.parseMediaType(contentType)
          : MediaType.APPLICATION_OCTET_STREAM;

      builder.part("file", resource)
          .contentType(mediaType);

      builder.part("language", "eng");
      builder.part("isOverlayRequired", "false");
      builder.part("scale", "true");
      builder.part("OCREngine", "2");

      return restClient.post()
          .uri("/parse/image")
          .header("apikey", apiKey)
          .contentType(MediaType.MULTIPART_FORM_DATA)
          .body(builder.build())
          .retrieve()
          .body(OcrSpaceResponse.class);
    } catch (IOException | RestClientException e) {
      log.warn("OCR Space API 호출에 실패했습니다.", e);
      return null;
    }
  }
}
