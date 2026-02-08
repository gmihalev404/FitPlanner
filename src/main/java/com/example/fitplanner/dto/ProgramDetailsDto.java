package com.example.fitplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramDetailsDto implements Serializable {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String difficulty;
    private Double rating;
    private String trainerName;
    private List<WorkoutSessionDto> sessions;
}

