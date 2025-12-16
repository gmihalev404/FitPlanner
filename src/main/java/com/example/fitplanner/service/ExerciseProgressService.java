package com.example.fitplanner.service;

import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.repository.ExerciseProgressRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Transactional
@Service
public class ExerciseProgressService {

    private final ExerciseProgressRepository exerciseProgressRepository;

    @Autowired
    public ExerciseProgressService(ExerciseProgressRepository exerciseProgressRepository) {
        this.exerciseProgressRepository = exerciseProgressRepository;
    }

    public List<ExerciseProgress> getByProgramAndDay(Long programId, LocalDate day) {
        return exerciseProgressRepository.getByProgramIdAndDay(programId, day);
    }

    public ExerciseProgress save(ExerciseProgress exerciseProgress) {
        return exerciseProgressRepository.save(exerciseProgress);
    }

    public ExerciseProgress getById(Long id) {
        return exerciseProgressRepository.findById(id).orElse(null);
    }
}