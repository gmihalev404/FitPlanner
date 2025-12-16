package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.ExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExerciseProgressRepository extends JpaRepository<ExerciseProgress, Long> {

    @Query("SELECT exPr FROM ExerciseProgress exPr WHERE exPr.workoutSession.program.id = :programId AND exPr.lastScheduled = :day")
    List<ExerciseProgress> getByProgramIdAndDay(@Param("programId") Long programId,
                                                 @Param("day") LocalDate day);
}