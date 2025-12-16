package com.example.fitplanner.entity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Program extends BaseEntity{
    @Column
    @Size(min = 2, max = 64)
    String name;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "program")
    private Set<WorkoutSession> sessions = new HashSet<>() ;

    @Column
    private LocalDateTime createdAt;
}