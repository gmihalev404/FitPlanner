package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.ExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseProgressRepository extends JpaRepository<ExerciseProgress, Long> {
}
