package com.example.fitplanner.entity.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Quote extends BaseEntity{

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private String author;

    public Quote(String text, String author) {
        this.text = text;
        this.author = author;
    }
}