package com.example.fitplanner.service;

import com.example.fitplanner.dto.CreatedProgramDto;
import com.example.fitplanner.dto.DayWorkout;
import com.example.fitplanner.dto.ExerciseProgressDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.entity.model.*;
import com.example.fitplanner.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExerciseProgressRepository exerciseProgressRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    @Autowired
    public ProgramService(ProgramRepository programRepository, WorkoutSessionRepository workoutSessionRepository, ExerciseProgressRepository exerciseProgressRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.programRepository = programRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    public List<Program> getProgramsByUserId(Long userId) {
        return programRepository.getByUserId(userId);
    }

    public Program getCurrentProgram() {
        return programRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
    }

    public Program getProgramById(Long id) {
        return programRepository.findById(id).orElse(null);
    }

    public Program saveProgram(Program program) {
        return programRepository.save(program);
    }

    public void deleteProgram(Long id) {
        programRepository.deleteById(id);
    }

    public void createProgram(CreatedProgramDto dto, UserDto userDto) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + userDto.getId()));

        Program program = new Program(dto.getName(), user);
        program = programRepository.save(program);

        LocalDate today = LocalDate.now();

        // Determine total weeks to schedule
        int totalWeeks;
        if (!dto.getRepeats() || dto.getScheduleMonths() == 0) totalWeeks = 1; // Only schedule once
        else totalWeeks = dto.getScheduleMonths() * 4; // Approximate 4 weeks per month

        for (int weekOffset = 0; weekOffset < totalWeeks; weekOffset++) {
            // Start of the target week
            LocalDate targetWeekStart = today.plusWeeks(weekOffset);

            for (DayWorkout dayWorkout : dto.getWeekDays()) {
                if (dayWorkout.getExercises() == null || dayWorkout.getExercises().isEmpty()) continue;
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayWorkout.getDay().toUpperCase());

                // Calculate session date
                LocalDate sessionDate = targetWeekStart.with(dayOfWeek);

                // Ensure session date is not in the past
                if (!dto.getRepeats() && sessionDate.isBefore(today)) {
                    sessionDate = sessionDate.plusWeeks(1);
                } else if (dto.getRepeats() && sessionDate.isBefore(today)) {
                    sessionDate = today.with(dayOfWeek);
                    if (sessionDate.isBefore(today)) {
                        sessionDate = sessionDate.plusWeeks(1);
                    }
                }

                // Create WorkoutSession
                WorkoutSession session = new WorkoutSession(program, sessionDate);
                program.addSession(session);
                workoutSessionRepository.save(session);

                // Add exercises to session
                for (ExerciseProgressDto epDto : dayWorkout.getExercises()) {
                    Exercise exercise = exerciseRepository.findById(epDto.getExerciseId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Exercise not found with id: " + epDto.getExerciseId()));
                    ExerciseProgress progress = new ExerciseProgress(
                            session,
                            exercise,
                            user,
                            epDto.getReps(),
                            epDto.getSets(),
                            epDto.getWeight(),
                            sessionDate
                    );
                    session.addExercise(progress);
                    exerciseProgressRepository.save(progress);
                }
            }
        }
    }
}