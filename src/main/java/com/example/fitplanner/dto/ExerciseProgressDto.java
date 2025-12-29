package com.example.fitplanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ExerciseProgressDto implements Serializable {
    private Long exerciseId;

    private String name;
    private Integer reps;
    private Integer sets;
    private Double weight;
}