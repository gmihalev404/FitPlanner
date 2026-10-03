package com.example.fitplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FitPlannerApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FitPlannerApplication.class);

        app.setApplicationStartup(new BufferingApplicationStartup(2048));

        app.run(args);
    }
}