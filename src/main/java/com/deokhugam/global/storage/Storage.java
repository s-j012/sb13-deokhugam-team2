package com.deokhugam.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface Storage {

  String upload(MultipartFile file);

  void delete(String path);

  String getUrl(String path);
}
