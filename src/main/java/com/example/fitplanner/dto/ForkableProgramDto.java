package com.example.fitplanner.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForkableProgramDto {
    private Long id;
    private String name;
    private String descriptionShort;
    private String imageUrl;
    private String difficulty;
    private Double rating;
    private String trainerName;
}
