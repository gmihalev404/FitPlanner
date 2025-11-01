package com.example.fitplanner.entity.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class ExerciseProgress extends BaseEntity{
    @ManyToOne
    private User user;

    @ManyToOne
    private Exercise exercise;

    @Min(0)
    @Max(100_000)
    private Integer time; // in seconds

    @Min(0)
    @Max(100)
    private Integer repetitions;

    @Min(0)
    @Max(500)
    private Double weight; // in kg

    private LocalDate lastScheduled;

    private LocalDate lastCompleted;

    private Integer completedExercisesInARow = 0;

    private Boolean suggestedIncrease = false;

    private Boolean completed;
}