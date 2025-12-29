package com.example.fitplanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@Data
@NoArgsConstructor
public class ExerciseProgressDto implements Serializable {
    private Long exerciseId;

    private Long id;
    private String name;
    private Integer reps;
    private Integer sets;
    private Double weight;

    private static final AtomicLong counter = new AtomicLong(1);

    public ExerciseProgressDto(Long exerciseId, String name, Integer reps, Integer sets, Double weight) {
        this.exerciseId = exerciseId;
        this.name = name;
        this.reps = reps;
        this.sets = sets;
        this.weight = weight;

        this.id = counter.getAndIncrement();
    }
}