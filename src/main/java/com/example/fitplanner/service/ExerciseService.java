package com.example.fitplanner.service;

import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.dto.ExerciseProgressDto;
import com.example.fitplanner.dto.SearchExerciseDto;
import com.example.fitplanner.dto.StatsExerciseDto;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.repository.ExerciseProgressRepository;
import com.example.fitplanner.repository.ExerciseRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final ExerciseProgressRepository exerciseProgressRepository;
    private final ModelMapper modelMapper;
    private final double LB_TO_KG = 0.45359237;
    private final double KG_TO_LB = 2.20462262;

    @Autowired
    public ExerciseService(ExerciseRepository exerciseRepository,
                           ExerciseProgressRepository exerciseProgressRepository
            , ModelMapper modelMapper) {
        this.exerciseRepository = exerciseRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.modelMapper = modelMapper;
    }

    public List<ExerciseDto> getAll(){
        List<Exercise> exercises = exerciseRepository.getAll();
        List<ExerciseDto> exerciseDtos = new LinkedList<>();
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

    public List<ExerciseProgressDto> getAllProgresses(Long userId) {
        List<ExerciseProgress> exerciseProgresses = exerciseProgressRepository.findByUserId(userId);
        List<ExerciseProgressDto> dtos = new ArrayList<>();
        for (ExerciseProgress progress : exerciseProgresses) {
            dtos.add(modelMapper.map(progress, ExerciseProgressDto.class));
        }
        return dtos;
    }

    public List<StatsExerciseDto> getConvertedStats(Long userId, String unitPreference) {
        List<ExerciseProgress> entities = exerciseProgressRepository.findByUserId(userId);
        boolean isLbs = "lbs".equalsIgnoreCase(unitPreference);

        return entities.stream()
                .map(entity -> {
                    StatsExerciseDto dto = modelMapper.map(entity, StatsExerciseDto.class);
                    if (isLbs && dto.getWeight() != null) {
                        double convertedWeight = dto.getWeight() * KG_TO_LB;
                        dto.setWeight(Math.round(convertedWeight * 100.0) / 100.0);
                    }
                    return dto;
                })
                .filter(dto -> dto.getCompletedDate() != null)
                .sorted(Comparator.comparing(StatsExerciseDto::getCompletedDate))
                .collect(Collectors.toList());
    }

    // Inside ExerciseService.java
    public List<SearchExerciseDto> searchExercises(String query) {
        List<Exercise> entities = (query == null || query.isBlank())
                ? exerciseRepository.findAll()
                : exerciseRepository.searchExercises(query);

        return entities.stream()
                .map(this::mapToDto)
                .toList();
    }

    private SearchExerciseDto mapToDto(Exercise entity) {
        return SearchExerciseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .targetMuscle(entity.getCategory().name())
                .imageUrl(entity.getImageUrl())
                .equipment(entity.getEquipmentType().name())
                .build();
    }
}