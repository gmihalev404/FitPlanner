package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Role;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Role role;
    private String theme = "dark";
    private String language = "en";
}
