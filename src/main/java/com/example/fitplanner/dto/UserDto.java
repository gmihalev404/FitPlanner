package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.Notification;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserDto implements Serializable {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private Role role;
    private String theme = "dark";
    private String language = "en";
    private String measuringUnits = "kg";
    private String profileImageUrl;
    private List<NotificationDto> notifications = new ArrayList<>();
    private Difficulty experience;
}
