package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Category;
import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.ExerciseType;
import com.example.fitplanner.entity.enums.EquipmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.stereotype.Component;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Exercise extends BaseEntity {
    @NotBlank
    @Size(min = 3, max = 32)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private ExerciseType exerciseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private EquipmentType equipmentType;

    @NotBlank
    @Column(nullable = false)
    private String imageUrl;

    @NotBlank
    @Column(nullable = false)
    private String videoUrl;

    public Exercise(String name, Category category, ExerciseType exerciseType,
                    EquipmentType equipmentType, String imageUrl, String videoUrl) {
        this.name = name;
        this.category = category;
        this.exerciseType = exerciseType;
        this.equipmentType = equipmentType;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }
}