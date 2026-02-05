package com.example.fitplanner.dto;

import lombok.Data;
import java.io.Serializable;

import lombok.Data;
import java.io.Serializable;

@Data
public class ExerciseSessionResultDto implements Serializable {
    private Long exerciseId;       // ID of ExerciseProgress
    private boolean finished;
    private int reps;
    private int sets;
    private double weight;
    private int setsCompleted = 0;
}