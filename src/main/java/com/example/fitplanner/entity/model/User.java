package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @NotBlank
    @Size(min = 2, max = 24)
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 24)
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Min(10)
    @Max(120)
    @Column(nullable = false)
    private Integer age;

    @Min(20)
    @Max(300)
    @Column(nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty experience;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    @NotBlank
    @Size(min = 4)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate createdAt = LocalDate.now();

    @Column(nullable = false)
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

    public User(String firstName, String lastName, String username, Role role, Gender gender,
                Integer age, Double weight, Difficulty experience, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
        this.experience = experience;
        this.email = email;
        this.password = password;
    }

    public void updatePreferences(String theme, String language, String units) {
        this.theme = theme;
        this.language = language;
        this.measuringUnits = units;
    }
}