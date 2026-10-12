package com.harenopost.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 업로드 이미지(외부 절대경로)를 /upload/** 로 제공 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String location = Path.of(uploadDir).toAbsolutePath().toUri().toString(); // file:///.../upload/
		registry.addResourceHandler("/upload/**").addResourceLocations(location.endsWith("/") ? location : location + "/");
	}
}
