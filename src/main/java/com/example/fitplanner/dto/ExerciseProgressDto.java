package com.example.fitplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ExerciseProgressDto implements Serializable {
    private String name;
    private double weight;
    private int reps;
    private int sets;
}