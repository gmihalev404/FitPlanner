package com.example.fitplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {
    private String query;
    private String category;
    private String difficulty;
}