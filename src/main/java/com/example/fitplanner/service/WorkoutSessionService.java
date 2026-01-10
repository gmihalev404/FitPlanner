package com.example.fitplanner.service;

import com.example.fitplanner.dto.DateWorkout;
import com.example.fitplanner.dto.ExerciseProgressDto;
import com.example.fitplanner.entity.model.WorkoutSession;
import com.example.fitplanner.repository.WorkoutSessionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkoutSessionService {
    final private WorkoutSessionRepository workoutSessionRepository;
    final private ModelMapper modelMapper;

    private final double LB_TO_KG = 0.45359237;
    private final double KG_TO_LB = 2.20462262;


    public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository, ModelMapper modelMapper) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.modelMapper = modelMapper;
    }

    public List<ExerciseProgressDto> getWorkoutsByProgramIdsAndDate(List<Long> programIds, LocalDate date, String units){
        if(programIds == null || programIds.isEmpty()) return new ArrayList<>();
        System.out.println(date +" " + programIds.stream().map(String::valueOf).collect(Collectors.joining(" ")));
        List<WorkoutSession> sessions = workoutSessionRepository.getByProgramIdsAndDate(programIds, date);
        List<DateWorkout> dtos = sessions.stream()
                .map(session -> modelMapper.map(session, DateWorkout.class))
                .toList();
        List<ExerciseProgressDto> exerciseProgressDtos = new ArrayList<>();
        for (DateWorkout dw : dtos) {
            exerciseProgressDtos.addAll(dw.getExercises());
        }
        if(units.equals("lbs")){
            for (ExerciseProgressDto dto : exerciseProgressDtos) {
                dto.setWeight(dto.getWeight() * KG_TO_LB);
            }
        }
        return exerciseProgressDtos;
    }
}