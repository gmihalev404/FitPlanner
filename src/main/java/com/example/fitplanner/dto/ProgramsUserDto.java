package com.example.fitplanner.dto;

import com.example.fitplanner.entity.enums.Role;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProgramsUserDto implements Serializable{
    private Long id;

    private String theme = "dark";
    private String language = "en";
    private String measuringUnits = "kg";
    private List<Long> programIds = new ArrayList<>();

}