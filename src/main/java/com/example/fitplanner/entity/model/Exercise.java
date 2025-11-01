package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Category;
import com.example.fitplanner.entity.enums.ExerciseType;
import com.example.fitplanner.entity.enums.EquipmentType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Exercise extends BaseEntity{
    @NotBlank
    @Size(min = 3, max = 32)
    private String name;

    @Size(max = 500)
    private String description;

    @Enumerated(value = EnumType.STRING)
    private Category category;

    @Enumerated(value = EnumType.STRING)
    private ExerciseType exerciseType;

    @Enumerated(value = EnumType.STRING)
    private EquipmentType equipmentType;

    @NotBlank
    @Size(max = 32)
    private String imageUrl;

    @NotBlank
    @Size(max = 32)
    private String videoUrl;
}