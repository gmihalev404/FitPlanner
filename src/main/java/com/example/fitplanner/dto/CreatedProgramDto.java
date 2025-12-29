package com.example.fitplanner.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
public class CreatedProgramDto implements Serializable {
    private String name;
    private Boolean repeats = true;
    private Integer scheduleMonths = 6;
    private Boolean notifications = true;
    private Boolean isPublic = false;

    private List<DayWorkout> weekDays = new ArrayList<>();
}