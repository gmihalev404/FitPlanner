package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class User extends BaseEntity{
    @NotBlank
    @Size(min = 2, max = 24)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 24)
    private String lastName;

    @NotBlank
    @Size(max = 64)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Min(10)
    @Max(120)
    private Integer age;

    @Min(20)
    @Max(300)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty experience;

    @Email
    private String email;

    @NotBlank
    @Size(min = 4)
    private String password;

    private LocalDate createdAt = LocalDate.now();

    private LocalDate lastUpdated = LocalDate.now();

    @OneToMany(mappedBy = "user")
    private Set<Program> programs = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ExerciseProgress> completedExercises = new HashSet<>();

    private String profileImageUrl;

    @Column(nullable = false)
    private String theme = "dark";

    @Column(nullable = false)
    private String language = "en";

    @Column(nullable = false)
    private String measuringUnits = "kg";
}