package com.example.fitplanner.entity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString(exclude = {"user", "workoutSession"})
@NoArgsConstructor
public class ExerciseProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_id", nullable = false)
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
    private Integer missedExercisesInARow = 0;

    @Column
    private Boolean suggestedChangeIncrease = false;

    @Column
    private Double suggestedChange = 0.0;

    @Column(nullable = false)
    @NotNull
    private Boolean completed = false;

    @Column(nullable = false)
    @NotNull
    @Min(0)
    private Integer setsCompleted = 0;

    public ExerciseProgress(WorkoutSession workoutSession, Exercise exercise, User user,
                            Integer reps, Integer sets, Double weight, LocalDate lastScheduled) {
        this.workoutSession = workoutSession;
        this.exercise = exercise;
        this.user = user;
        this.reps = reps;
        this.sets = sets;
        this.weight = weight;
        this.lastScheduled = lastScheduled != null ? lastScheduled : LocalDate.now(); // fallback
        this.completed = false;
        this.completedExercisesInARow = 0;
        this.missedExercisesInARow = 0;
        this.suggestedChangeIncrease = null;
        this.suggestedChange = null;

        if (workoutSession != null) workoutSession.addExercise(this);
    }

    public void markCompleted(LocalDate date) {
        this.completed = true;
        this.lastCompleted = date;
        this.completedExercisesInARow++;
        this.missedExercisesInARow = 0;
    }

    public void markMissed(LocalDate date) {
        this.completed = false;
        this.lastScheduled = date;
        this.missedExercisesInARow++;
        this.completedExercisesInARow = 0;
    }

    public void applySuggestedChange(boolean accepted) {
        if (accepted && suggestedChange != null) {
            if (Boolean.TRUE.equals(suggestedChangeIncrease)) {
                this.weight = this.weight * (1 + suggestedChange / 100);
            } else {
                this.weight = this.weight * (1 - suggestedChange / 100);
            }
        }
        // след прилагане или отказ
        this.suggestedChangeIncrease = null;
        this.suggestedChange = null;
    }

    public void resetCountersAfterSuggestion() {
        this.completedExercisesInARow = 0;
        this.missedExercisesInARow = 0;
        this.suggestedChangeIncrease = null;
        this.suggestedChange = 0.0;
    }
}