package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e")
    Set<Exercise> getAll();

//    @Query("SELECT e FROM Exercise e WHERE e.id = :id")
//    Optional<Exercise> getById(@Param(value = "id") Long id);
}
