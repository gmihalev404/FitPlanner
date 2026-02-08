package com.example.fitplanner.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainerSearchDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String profileImageUrl;
    private String experience;

}