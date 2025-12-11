package com.example.fitplanner.dto;
import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Data
public class UserRegisterDto implements Serializable {
    @NotBlank
    @Size(min = 2, message = "First name MUST be at least 2 characters")
    @Size(max = 24, message = "First name MUST be at most 24 characters")
    private String firstName;

    @NotBlank
    @Size(min = 2, message = "Last name MUST be at least 2 characters")
    @Size(max = 24, message = "Last name MUST be at most 24 characters")
    private String lastName;

    @NotBlank
    @Size(min = 4, message = "Username MUST be at least 4 characters")
    @Size(max = 64, message = "Username MUST be at most 64 characters")
    private String username;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Gender MUST be selected")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Experience MUST be selected")
    private Difficulty experience;

    @Min(10)
    @Max(120)
    private Integer age;

    @Min(20)
    @Max(300)
    private Double weight;

    @Email
    private String email;

    @NotBlank
    @Size(min = 4, message = "Password MUST be at least 4 characters")
    private String password;

    @NotBlank
    private String confirmPassword;
}