package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
public class ProfileUserDto implements Serializable {
    //no change
    private Long id;

    private String username;

    private String firstName;

    private String lastName;

//    private String password;

    //no change
    private Role role;

    //in settings
    private String theme = "dark";
    private String language = "en";
    private String measuringUnits = "kg";

    //no change here
    private Gender gender;

    //no change here
    private Integer age;

    private Double weight;

    //no change here
    private Difficulty experience;

    //no change here (?)
    private String email;
    private String password;

    //no change here
    private LocalDate createdAt;
    //no change here
    private LocalDate lastUpdated;

    private String profileImageUrl;
}