package com.example.fitplanner.service;

import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.repository.ExerciseRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

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
        for (Exercise e : exercises) {
            exerciseDtos.add(modelMapper.map(e, ExerciseDto.class));
        }
        return exerciseDtos;
    }

    public ExerciseDto getById(Long id) {
        Exercise exercise = exerciseRepository.getById(id);
        ExerciseDto exerciseDto = modelMapper.map(exercise, ExerciseDto.class);
        return exerciseDto;
    }
}
