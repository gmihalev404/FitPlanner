package com.example.fitplanner.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class QuoteDto {
    private Long id;
    private String text;

    private String author;

    public QuoteDto(String text, String author) {
        this.text = text;
        this.author = author;
    }
}