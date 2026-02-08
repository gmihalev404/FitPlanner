package com.example.fitplanner.dto;

import lombok.Data;
import java.io.Serializable;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ExerciseSessionDto implements Serializable {
    private Long progressId;       // ExerciseProgress ID
    private Long exerciseId;       // Exercise ID
    private String name;
    private String programName;
    private double weight;
    private int reps;
    private int sets;

    private int setsCompleted = 0; // populated from DB
    private boolean finished;
    private Boolean increaseAccepted; // null = not chosen, true/false = chosen
    private Boolean suggestedIncrease; // true = increase, false = decrease, null = no suggestion
    private Double suggestedChange; // +5% or -5%
}
