package com.example.fitplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/icons/**")
                .addResourceLocations("classpath:/static/icons/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");

        registry.addResourceHandler("/videos/**")
                .addResourceLocations("classpath:/static/videos/");

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }

@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new UserLocaleInterceptor())
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
