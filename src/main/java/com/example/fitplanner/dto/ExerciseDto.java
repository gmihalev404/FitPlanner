package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Category;
import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.EquipmentType;
import com.example.fitplanner.entity.enums.ExerciseType;
import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@ToString
public class ExerciseDto implements Serializable {
    private Long id;

    private String name;

    private String description;

    private Category category;

    private ExerciseType exerciseType;

    private EquipmentType equipmentType;

    private String imageUrl;

    private String videoUrl;
    private LocalDate getLastCompleted;
}