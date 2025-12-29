package com.example.fitplanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DayWorkout implements Serializable {
    private String day;
    private List<ExerciseProgressDto> exercises = new ArrayList<>();

    public DayWorkout(String day) {
        this.day = day;
        this.exercises = new ArrayList<>();
    }

    public DayWorkout(String day, List<ExerciseProgressDto> exercises) {
        this.day = day;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }
}
