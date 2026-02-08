package com.example.fitplanner.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchExerciseDto {
    private Long id;
    private String name;
    private String targetMuscle;
    private String imageUrl;   // Optional image for the exercise
    private String equipment;  // Useful for quick info on the card
}
