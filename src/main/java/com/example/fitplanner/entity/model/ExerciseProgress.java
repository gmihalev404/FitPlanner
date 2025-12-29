package com.example.fitplanner.entity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class ExerciseProgress extends BaseEntity {

    @ManyToOne(optional = false)
    @NotNull
    private WorkoutSession workoutSession;

    @ManyToOne(optional = false)
    @NotNull
    private Exercise exercise;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(100)
    private Integer reps;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(10)
    private Integer sets;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(500)
    private Double weight;

    @Column(nullable = false)
    @NotNull
    private LocalDate lastScheduled;

    private LocalDate lastCompleted;

    @Column(nullable = false)
    @NotNull
    private Integer completedExercisesInARow = 0;

    @Column(nullable = false)
    @NotNull
    private Boolean suggestedIncrease = false;

    @Column(nullable = false)
    @NotNull
    private Boolean completed = false;

    public ExerciseProgress(WorkoutSession workoutSession, Exercise exercise, User user,
                            Integer reps, Integer sets, Double weight, LocalDate lastScheduled) {
        this.workoutSession = workoutSession;
        this.exercise = exercise;
        this.user = user;
        this.reps = reps;
        this.sets = sets;
        this.weight = weight;
        this.lastScheduled = lastScheduled;
        if (workoutSession != null) {
            workoutSession.addExercise(this);
        }
    }

    public void markCompleted(LocalDate date) {
        this.completed = true;
        this.lastCompleted = date;
        this.completedExercisesInARow++;
    }

    public void updateProgress(Integer reps, Integer sets, Double weight) {
        this.reps = reps;
        this.sets = sets;
        this.weight = weight;
    }
}