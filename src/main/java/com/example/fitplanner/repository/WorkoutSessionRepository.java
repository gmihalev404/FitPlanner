package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.entity.model.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    // Matches your Entity's field: scheduledFor
    Optional<WorkoutSession> findByUserIdAndScheduledFor(Long userId, LocalDate date);

    @Query("SELECT COALESCE(s.finished, false) FROM WorkoutSession s WHERE s.user.id = :userId AND s.scheduledFor = :date")
    boolean isSessionFinished(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.program.id IN :programIds AND ws.scheduledFor = :date")
    List<WorkoutSession> getByProgramIdsAndDate(@Param("programIds") List<Long> programIds, @Param("date") LocalDate date);

    @Query("""
    SELECT ws
    FROM WorkoutSession ws
    WHERE ws.program = :program
      AND ws.scheduledFor > :localDate
""")
    List<WorkoutSession> findAllByProgramAndSessionDateAfter(
            @Param("program") Program program,
            @Param("localDate") LocalDate localDate
    );

    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user.id = :userId AND ws.scheduledFor = :date")
    Optional<WorkoutSession> findSession(@Param("userId") Long userId, @Param("date") LocalDate date);}