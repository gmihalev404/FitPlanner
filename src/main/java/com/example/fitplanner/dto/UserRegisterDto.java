package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import org.springframework.stereotype.Component;

@Component
public class UserRegisterDto {
    @NotBlank
    @Size(min = 2, max = 24)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 24)
    private String lastName;

    @NotBlank
    @Size(max = 64)
    private String username;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Min(10)
    @Max(120)
    private Integer age;

    @Min(20)
    @Max(300)
    private Double weight;

    @Min(0)
    @Max(50)
    private Integer yearsOfExperience;

    @Email
    private String email;

    @NotBlank
    @Size(min = 4)
    private String password;
}