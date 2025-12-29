package com.example.fitplanner.entity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class WorkoutSession extends BaseEntity {
    @ManyToOne(optional = false)
    @NotNull
    private Program program;

    @Column(nullable = false)
    private LocalDate scheduledFor;

    @OneToMany(mappedBy = "workoutSession")
    private Set<ExerciseProgress> exercises = new HashSet<>();

    public WorkoutSession(Program program, LocalDate scheduledFor) {
        this.program = program;
        this.scheduledFor = scheduledFor;
    }

    public void addExercise(ExerciseProgress progress) {
        exercises.add(progress);
    }
}