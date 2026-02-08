package com.example.fitplanner.dto;

import lombok.Data;

import java.util.List;

@Data
public class WorkoutSessionDto {
    private Long id;
    private String date;
    private boolean finished;
    private List<ExerciseSessionDto> exercises;
}
