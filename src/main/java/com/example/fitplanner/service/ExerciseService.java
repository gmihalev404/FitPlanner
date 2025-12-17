package com.example.fitplanner.service;

import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.repository.ExerciseRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository, ModelMapper modelMapper) {
        this.exerciseRepository = exerciseRepository;
        this.modelMapper = modelMapper;
    }

    public Set<ExerciseDto> getAll(){
        Set<Exercise> exercises = exerciseRepository.getAll();
        Set<ExerciseDto> exerciseDtos = new LinkedHashSet<>();
        System.out.println("service" + exercises.stream().map(Exercise::toString).collect(Collectors.joining(", ")));
        for (Exercise e : exercises) {
            exerciseDtos.add(modelMapper.map(e, ExerciseDto.class));
        }
        System.out.println("service" + exerciseDtos.stream().map(ExerciseDto::toString).collect(Collectors.joining(", ")));
        return exerciseDtos;
    }

    public ExerciseDto getById(Long id) {
        Exercise exercise = exerciseRepository.getById(id);
        ExerciseDto exerciseDto = modelMapper.map(exercise, ExerciseDto.class);
        return exerciseDto;
    }
}
