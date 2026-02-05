package com.example.fitplanner.service;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.model.*;
import com.example.fitplanner.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
    private final ModelMapper modelMapper;

    private final double LB_TO_KG = 0.45359237;

    @Autowired
    public ProgramService(ProgramRepository programRepository,
                          WorkoutSessionRepository workoutSessionRepository,
                          ExerciseProgressRepository exerciseProgressRepository,
                          ExerciseRepository exerciseRepository,
                          UserRepository userRepository,
                          ModelMapper modelMapper) {
        this.programRepository = programRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<ProgramDto> getProgramsByUserId(Long userId) {
        List<Program> programs = programRepository.getByUserId(userId);
        List<ProgramDto> programDtos = new ArrayList<>();
        for (Program p : programs) {
            ProgramDto programDto = modelMapper.map(p, ProgramDto.class);
            for (WorkoutSession ws : p.getSessions()) {
                List<ExerciseProgressDto> dtos = ws.getExercises()
                        .stream()
                        .map(e -> modelMapper.map(e, ExerciseProgressDto.class))
                        .toList();
                programDto.getWorkouts().add(new DateWorkout(ws.getScheduledFor(), dtos));
            }
            programDtos.add(programDto);
        }
        return programDtos;
    }

    public void createProgram(CreatedProgramDto dto, ProgramsUserDto userDto, String units) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Program program = new Program(dto.getName(), user, dto.getScheduleMonths(), dto.getNotifications(), dto.getIsPublic());
        program = programRepository.save(program);

        generateSessions(program, dto, units, user);
    }

    public void updateProgram(Long programId, CreatedProgramDto dto, String units) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Program not found: " + programId));

        program.setName(dto.getName());
        program.setScheduleMonths(dto.getScheduleMonths());
        program.setNotifications(dto.getNotifications());
        program.setIsPublic(dto.getIsPublic());
        program.setLastChanged(LocalDateTime.now());
        programRepository.save(program);

        LocalDate today = LocalDate.now();

        // 1. Clear future sessions to avoid overlaps during rescheduling
        List<WorkoutSession> futureSessions = workoutSessionRepository.findAllByProgramAndSessionDateAfter(program, today);
        workoutSessionRepository.deleteAll(futureSessions);
        workoutSessionRepository.flush();

        // 2. Regenerate sessions based on new configuration
        generateSessions(program, dto, units, program.getUser());
    }

    /**
     * Core logic to generate WorkoutSessions and ExerciseProgress records safely.
     */
    private void generateSessions(Program program, CreatedProgramDto dto, String units, User user) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        int totalWeeks = (!dto.getRepeats() || dto.getScheduleMonths() == 0) ? 1 : dto.getScheduleMonths() * 4;

        for (int weekOffset = 0; weekOffset < totalWeeks; weekOffset++) {
            LocalDate targetWeekStart = startOfWeek.plusWeeks(weekOffset);

            for (DayWorkout dayWorkout : dto.getWeekDays()) {
                if (dayWorkout.getExercises() == null || dayWorkout.getExercises().isEmpty()) continue;

                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayWorkout.getDay().toUpperCase());
                LocalDate sessionDate = targetWeekStart.with(dayOfWeek);

                // Skip past dates based on repeat logic
                if (sessionDate.isBefore(today)) {
                    if (dto.getRepeats()) continue;
                    else sessionDate = sessionDate.plusWeeks(1);
                }

                // FIX: Check if session exists for this user/date to prevent DuplicateEntry error
                final LocalDate finalSessionDate = sessionDate;
                WorkoutSession session = workoutSessionRepository
                        .findSession(user.getId(), finalSessionDate)
                        .orElseGet(() -> {
                            WorkoutSession newSession = new WorkoutSession();
                            newSession.setProgram(program);
                            newSession.setUser(user);
                            newSession.setScheduledFor(finalSessionDate);
                            newSession.setFinished(false);
                            return workoutSessionRepository.save(newSession);
                        });

                // Attach session to program if not already attached
                if (session.getProgram() == null || !session.getProgram().getId().equals(program.getId())) {
                    session.setProgram(program);
                    workoutSessionRepository.save(session);
                }

                for (ExerciseProgressDto epDto : dayWorkout.getExercises()) {
                    Exercise exercise = exerciseRepository.findById(epDto.getExerciseId())
                            .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

                    // Avoid duplicate ExerciseProgress records within the same session
                    boolean exists = session.getExercises().stream()
                            .anyMatch(ep -> ep.getExercise().getId().equals(epDto.getExerciseId()));

                    if (!exists) {
                        double weight = "lbs".equals(units) ? epDto.getWeight() * LB_TO_KG : epDto.getWeight();
                        ExerciseProgress progress = new ExerciseProgress(
                                session,
                                exercise,
                                user,
                                epDto.getReps(),
                                epDto.getSets(),
                                weight,
                                finalSessionDate
                        );
                        exerciseProgressRepository.save(progress);
                    }
                }
            }
        }
    }

    public void removeProgram(Long programId) {
        programRepository.deleteById(programId);
    }

    public <T> T getById(Long id, Class<T> dtoType) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
        return modelMapper.map(program, dtoType);
    }
}