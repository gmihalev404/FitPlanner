package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.*;
import lombok.Data;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Data
public class DetailedUserDto implements Serializable {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private Role role;
    private Gender gender;
    private Double weight;
    private Difficulty experience;
    private String email;
    private String password;
    private LocalDate createdAt = LocalDate.now();
    private LocalDate lastUpdated = LocalDate.now();
    private Set<DetailedProgramDto> programs = new LinkedHashSet<>();
    private List<ExerciseProgressDto> completedExercises = new LinkedList<>();
    private String profileImageUrl = "";
    private String theme = "dark";
    private String language = "en";
    private String measuringUnits = "kg";
    private List<WeightEntryDto> weightChanges = new ArrayList<>();
    private List<NotificationDto> notifications = new LinkedList<>();
    private Integer streak = 0;
    private LocalDate lastWorkoutDate;
    private Boolean enabled = true;
    private Set<UserDto> followers = new LinkedHashSet<>();
}
