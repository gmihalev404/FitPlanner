package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.ExerciseProgress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseProgressRepository extends JpaRepository<ExerciseProgress, Long> {

    @Query("SELECT exPr FROM ExerciseProgress exPr WHERE exPr.workoutSession.program.id = :programId AND exPr.lastScheduled = :day")
    List<ExerciseProgress> getByProgramIdAndDay(@Param("programId") Long programId,
                                                 @Param("day") LocalDate day);

    @Query("SELECT exPr FROM ExerciseProgress  exPr WHERE exPr.user.id = :userId")
    List<ExerciseProgress> findByUserId(@Param(value = "userId") Long userId);

    @Query("SELECT e FROM ExerciseProgress e " +
            "WHERE (:programIds IS NULL OR e.workoutSession.program.id IN :programIds) " +
            "AND e.lastScheduled = :date")
    List<ExerciseProgress> findByProgramIdsAndDate(List<Long> programIds, LocalDate date);

    @Query("SELECT e FROM ExerciseProgress e WHERE e.id = :exerciseId")
    ExerciseProgress getExerciseProgressById(@Param("exerciseId") Long exerciseId);

    @Query("SELECT ep FROM ExerciseProgress ep " +
            "WHERE ep.user.id = :userId " +
            "AND ep.exercise.id = :exerciseId " +
            "AND ep.lastScheduled = :lastScheduled")
    Optional<ExerciseProgress> findByUserIdAndExerciseIdAndLastScheduled(
            @Param("userId") Long userId,
            @Param("exerciseId") Long exerciseId,
            @Param("lastScheduled") LocalDate lastScheduled
    );

    @Query("SELECT SUM(ep.weight * ep.setsCompleted * ep.reps) FROM ExerciseProgress ep " +
            "WHERE ep.user.id = :userId AND ep.completed = true " +
            "AND ep.lastCompleted >= :startOfMonth")
    Double calculateMonthlyVolume(@Param("userId") Long userId, @Param("startOfMonth") LocalDate startOfMonth);

    // Count completed vs total scheduled for success rate
    @Query("SELECT COUNT(ep) FROM ExerciseProgress ep WHERE ep.user.id = :userId AND ep.lastScheduled >= :startOfMonth")
    long countScheduledThisMonth(@Param("userId") Long userId, @Param("startOfMonth") LocalDate startOfMonth);

    @Query("SELECT COUNT(ep) FROM ExerciseProgress ep WHERE ep.user.id = :userId AND ep.completed = true AND ep.lastCompleted >= :startOfMonth")
    long countCompletedThisMonth(@Param("userId") Long userId, @Param("startOfMonth") LocalDate startOfMonth);

    @Query("SELECT ep FROM ExerciseProgress ep " +
            "WHERE ep.user.id = :userId " +
            "AND ep.completed = true " +
            "ORDER BY ep.lastCompleted DESC")
    List<ExerciseProgress> findRecentCompletedExercises(@Param("userId") Long userId, Pageable pageable);}