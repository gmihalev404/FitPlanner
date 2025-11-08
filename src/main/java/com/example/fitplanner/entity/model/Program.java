package com.example.fitplanner.entity.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Program extends BaseEntity{
    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "program")
    private Set<WorkoutSession> sessions = new HashSet<>() ;
}