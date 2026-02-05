package com.example.fitplanner.entity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"program", "user", "exercises"})
@Table(
        name = "workout_sessions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "scheduled_for"})
        }
)
public class WorkoutSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "scheduled_for", nullable = false)
    private LocalDate scheduledFor;

    @Column(nullable = false)
    private boolean finished = false;

    @OneToMany(
            mappedBy = "workoutSession",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ExerciseProgress> exercises = new HashSet<>();

    public void addExercise(ExerciseProgress progress) {
        exercises.add(progress);
        progress.setWorkoutSession(this);
    }

    public void markFinished() {
        this.finished = true;
    }
}