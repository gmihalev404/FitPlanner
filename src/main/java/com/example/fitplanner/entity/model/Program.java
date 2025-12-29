package com.example.fitplanner.entity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Program extends BaseEntity {

    @Size(min = 2, max = 64)
    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @NotNull
    private User user;

    @OneToMany(mappedBy = "program")
    private Set<WorkoutSession> sessions = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Program(String name, User user) {
        this.name = name;
        this.user = user;
    }

    public void addSession(WorkoutSession session) {
        sessions.add(session);
    }
}