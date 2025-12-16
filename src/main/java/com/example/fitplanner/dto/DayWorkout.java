package com.example.fitplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class DayWorkout implements Serializable {
    private String day;
    private List<ExerciseProgressDto> exercises;
}
