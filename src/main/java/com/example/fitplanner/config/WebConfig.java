package com.example.fitplanner.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserLocaleInterceptor userLocaleInterceptor;
    private final Path uploadDir;

    public WebConfig(
            UserLocaleInterceptor userLocaleInterceptor,
            @Value("${app.upload-dir}") String uploadDir) {

        this.userLocaleInterceptor = userLocaleInterceptor;
        this.uploadDir = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String uploadLocation = uploadDir.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(userLocaleInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/icons/**",
                        "/videos/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "/webjars/**",
                        "/actuator/**",
                        "/error"
                );
    }
}
