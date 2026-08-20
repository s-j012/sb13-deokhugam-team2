package com.deokhugam.global.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(
    name = "deokhugam.storage.type",
    havingValue = "local",
    matchIfMissing = true
)
public class LocalStorageConfig implements WebMvcConfigurer {

  private final String rootPath;

  public LocalStorageConfig(
      @Value("${deokhugam.storage.local.root-path}") String rootPath
  ) {
    this.rootPath = rootPath;
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String location = Path.of(rootPath)
        .toAbsolutePath()
        .normalize()
        .toUri()
        .toString();

    registry
        .addResourceHandler("/storage/**")
        .addResourceLocations(location);
  }
}