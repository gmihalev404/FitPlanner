package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = {"programs", "completedExercises"})
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

    //trainer only todo
    // private Set<User> trainees = new HashSet<>();

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
}