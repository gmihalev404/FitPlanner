package com.example.fitplanner.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WorkoutResultWrapper implements Serializable {
    private List<ExerciseSessionResultDto> results;

    public List<ExerciseSessionResultDto> getResults() { return results; }
    public void setResults(List<ExerciseSessionResultDto> results) { this.results = results; }
}