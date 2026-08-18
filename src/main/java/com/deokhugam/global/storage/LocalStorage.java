package com.deokhugam.global.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "deokhugam.storage.type",
    havingValue = "local",
    matchIfMissing = true
)
public class LocalStorage implements Storage {

  private final Path rootPath;

  public LocalStorage(
      @Value("${deokhugam.storage.local.root-path}") String rootPath
  ) {
    this.rootPath = Path.of(rootPath)
        .toAbsolutePath()
        .normalize();
  }

  @Override
  public String upload(MultipartFile file) {
    try {
      Files.createDirectories(rootPath);

      String fileName = UUID.randomUUID() + getExtension(file.getOriginalFilename());
      Path targetPath = rootPath.resolve(fileName);

      file.transferTo(targetPath);

      return fileName;
    } catch (IOException e) {
      throw new IllegalStateException("파일 저장에 실패했습니다.", e);
    }
  }

  @Override
  public void delete(String path) {
    try {
      Files.deleteIfExists(rootPath.resolve(path));
    } catch (IOException e) {
      throw new IllegalStateException("파일 삭제에 실패했습니다.", e);
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
    return "/storage/" + path;
  }
}