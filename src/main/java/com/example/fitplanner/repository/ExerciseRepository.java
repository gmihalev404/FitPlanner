package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    @Query("SELECT e FROM Exercise e")
    List<Exercise> getAll();

    @Query("SELECT e FROM Exercise e WHERE " +
            "LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(e.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Exercise> searchExercises(@Param("query") String query);
}
