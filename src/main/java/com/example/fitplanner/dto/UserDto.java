package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Role;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserDto implements Serializable {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Role role;
    private String theme = "dark";
    private String language = "en";
}
