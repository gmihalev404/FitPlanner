package com.example.fitplanner.entity.model;

import com.example.fitplanner.entity.enums.Difficulty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"user"})
@NoArgsConstructor
public class Program extends BaseEntity {

//    @Size(min = 2, max = 64)
    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @NotNull
    private User user;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutSession> sessions = new LinkedList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime lastChanged = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean repeats = true;

    @Column(nullable = false)
    private Integer scheduleMonths = 6;

    @Column(nullable = false)
    private Boolean notifications = true;

    @Column(nullable = false)
    private Boolean isPublic = false;

    @Column
    private String description;

    @Column
    private String imageUrl;

    @Column
    private Difficulty difficulty = Difficulty.INTERMEDIATE;

    @Column
    private Double rating = 0.0;

    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long ratingCount = 0L;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProgramRating> ratings = new LinkedList<>();

    public Program(String name, User user, Integer scheduleMonths, Boolean notifications, Boolean isPublic, String imageUrl) {
        this.name = name;
        this.user = user;
        this.repeats = scheduleMonths != 0;
        this.scheduleMonths = scheduleMonths;
        this.notifications = notifications;
        this.isPublic = isPublic;
        this.imageUrl = imageUrl;
    }

    public void addSession(WorkoutSession session) {
        sessions.add(session);
    }

    public void updateRatingStats(double averageRating, long totalRatings) {
        this.rating = averageRating;
        this.ratingCount = totalRatings;
    }
}