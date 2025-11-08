package com.example.fitplanner.entity.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class WorkoutSession extends BaseEntity{
    @ManyToOne
    private Program program;

    private LocalDate scheduledFor;

    @OneToMany(mappedBy = "workoutSession")
    private Set<ExerciseProgress> exercises = new HashSet<>();
}
