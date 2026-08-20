package com.deokhugam.global.storage;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@ConditionalOnProperty(
    name = "deokhugam.storage.type",
    havingValue = "s3"
)
public class S3Storage implements Storage {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String bucket;
  private final long presignedUrlExpiration;

  public S3Storage(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${deokhugam.storage.s3.bucket}") String bucket,
      @Value("${deokhugam.storage.s3.presigned-url-expiration}") long presignedUrlExpiration
  ) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucket = bucket;
    this.presignedUrlExpiration = presignedUrlExpiration;
  }

  @Override
  public String upload(MultipartFile file) {
    String key = "book-thumbnails/"
        + UUID.randomUUID()
        + getExtension(file.getOriginalFilename());

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(file.getContentType())
        .build();

    try {
      s3Client.putObject(
          request,
          RequestBody.fromInputStream(
              file.getInputStream(),
              file.getSize()
          )
      );

      return key;
    } catch (IOException | S3Exception e) {
      throw new IllegalStateException("S3 파일 업로드에 실패했습니다.", e);
    }
  }

  @Override
  public void delete(String path) {
    DeleteObjectRequest request = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(path)
        .build();

    try {
      s3Client.deleteObject(request);
    } catch (S3Exception e) {
      throw new IllegalStateException("S3 파일 삭제에 실패했습니다.", e);
    }
  }

  private String getExtension(String originalFilename) {
    if (originalFilename == null) {
      return "";
    }

    int index = originalFilename.lastIndexOf(".");
    return index >= 0 ? originalFilename.substring(index) : "";
  }

  @Override
  public String getUrl(String path) {

    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(path)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
        .getObjectRequest(getObjectRequest)
        .build();

    return s3Presigner.presignGetObject(presignRequest)
        .url()
        .toString();
  }
}