package com.example.fitplanner.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//@Component
@Data
public class DateWorkout implements Serializable {
    private LocalDate date;
    private List<ExerciseProgressDto> exercises;

    public DateWorkout(LocalDate date, List<ExerciseProgressDto> exercises) {
        this.date = date;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }
}
