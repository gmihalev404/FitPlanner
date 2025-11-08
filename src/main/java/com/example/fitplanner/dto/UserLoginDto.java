package com.example.fitplanner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserLoginDto {
    @NotBlank
    @Size(max = 64)
    private String username;

    @Email
    private String email;

    @NotBlank
    @Size(min = 4)
    private String password;
}
