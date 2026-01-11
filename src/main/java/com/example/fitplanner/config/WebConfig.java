package com.example.fitplanner.config;

import org.springframework.context.annotation.Configuration;
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

//        registry.addResourceHandler("/**")
//                .addResourceLocations(
//                        "classpath:/static/",   // default Spring static folder
//                        "file:uploads/"         // your uploads folder
//                )
//                .setCachePeriod(0); // disable caching in dev
    }

//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        // Optional: map /favicon.ico to a default empty icon to avoid exceptions
//        registry.addViewController("/favicon.ico").setViewName("forward:/icons/default-favicon.ico");
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLocaleInterceptor());
    }
}