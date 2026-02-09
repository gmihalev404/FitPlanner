package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.entity.model.WorkoutSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Data
public class DetailedProgramDto implements Serializable {
    private String name;
    private User user;
    private List<WorkoutSession> sessions = new LinkedList<>();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastChanged = LocalDateTime.now();
    private Boolean repeats = true;
    private Integer scheduleMonths = 6;
    private Boolean notifications = true;
    private Boolean isPublic = false;
    private String description;
    private String imageUrl;
    private Difficulty difficulty = Difficulty.INTERMEDIATE;
    private Double rating = 0.0;
}
