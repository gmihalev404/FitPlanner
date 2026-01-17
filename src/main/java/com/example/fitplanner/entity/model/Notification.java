package com.example.fitplanner.entity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notification extends BaseEntity {
    @Column
    @NotNull
    private String name;

    @Column
    @NotNull
    private String description;

    @OneToOne
    private User sender;

    @ManyToOne
    private User observer;

    @Column
    private Boolean checked = false;

    @Column
    private LocalDateTime date = LocalDateTime.now();

    @Column
    private String url;
}