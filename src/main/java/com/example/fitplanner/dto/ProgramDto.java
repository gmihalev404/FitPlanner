package com.example.fitplanner.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Data
public class ProgramDto implements Serializable {
    private Long id;
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime lastChanged;
    private String imageUrl;

    private List<DateWorkout> workouts = new ArrayList<>();
}
