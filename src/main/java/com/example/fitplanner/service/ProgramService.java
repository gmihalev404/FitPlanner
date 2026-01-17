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
    private final double KG_TO_LB = 2.20462262;

    @Autowired
    public ProgramService(ProgramRepository programRepository, WorkoutSessionRepository workoutSessionRepository, ExerciseProgressRepository exerciseProgressRepository, ExerciseRepository exerciseRepository, UserRepository userRepository, ModelMapper modelMapper) {
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

    public <T> T getById(Long id, Class<T> dtoType) {
        Program program = getById(id);
        return modelMapper.map(program, dtoType);
    }

    public void createProgram(CreatedProgramDto dto, ProgramsUserDto userDto, String units) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Program program = new Program(dto.getName(), user, dto.getScheduleMonths(), dto.getNotifications(), dto.getIsPublic());
        program = programRepository.save(program);

        LocalDate today = LocalDate.now();
        // Start from Monday of the current week
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        // If it repeats, we want (months * 4) weeks.
        // If it doesn't repeat, we only want 1 week of sessions.
        int totalWeeks = (!dto.getRepeats() || dto.getScheduleMonths() == 0) ? 1 : dto.getScheduleMonths() * 4;

        for (int weekOffset = 0; weekOffset < totalWeeks; weekOffset++) {
            LocalDate targetWeekStart = startOfWeek.plusWeeks(weekOffset);

            for (DayWorkout dayWorkout : dto.getWeekDays()) {
                if (dayWorkout.getExercises() == null || dayWorkout.getExercises().isEmpty()) continue;

                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayWorkout.getDay().toUpperCase());
                LocalDate sessionDate = targetWeekStart.with(dayOfWeek);

                // --- REVISED LOGIC ---

                if (dto.getRepeats()) {
                    // If the date has passed, just skip this specific day for this week.
                    // The loop for the NEXT week (weekOffset + 1) will create the future session.
                    if (sessionDate.isBefore(today)) {
                        continue;
                    }
                } else {
                    // If not repeating and the day has passed, push it to next week
                    // so the user actually gets their workout.
                    if (sessionDate.isBefore(today)) {
                        sessionDate = sessionDate.plusWeeks(1);
                    }
                }

                // --- END REVISED LOGIC ---

                WorkoutSession session = new WorkoutSession(program, sessionDate);
                program.addSession(session);
                workoutSessionRepository.save(session);

                for (ExerciseProgressDto epDto : dayWorkout.getExercises()) {
                    Exercise exercise = exerciseRepository.findById(epDto.getExerciseId()).orElseThrow();
                    double weight = units.equals("lbs") ? epDto.getWeight() * LB_TO_KG : epDto.getWeight();

                    ExerciseProgress progress = new ExerciseProgress(
                            session, exercise, user, epDto.getReps(), epDto.getSets(), weight, sessionDate
                    );
                    exerciseProgressRepository.save(progress);
                }
            }
        }
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

        List<WorkoutSession> futureSessions = workoutSessionRepository.findAllByProgramAndSessionDateAfter(program, today.minusDays(1));
        workoutSessionRepository.deleteAll(futureSessions);

        workoutSessionRepository.flush();

        if (dto.getWeekDays() == null || dto.getWeekDays().isEmpty()) {
//            System.out.println("DEBUG: No weekdays found in DTO!");
            return;
        }

        int totalWeeks = (!dto.getRepeats() || dto.getScheduleMonths() == 0) ? 1 : dto.getScheduleMonths() * 4;

        for (int weekOffset = 0; weekOffset < totalWeeks; weekOffset++) {
            LocalDate referenceDate = today.plusWeeks(weekOffset);

            for (DayWorkout dayWorkout : dto.getWeekDays()) {
                if (dayWorkout.getExercises() == null || dayWorkout.getExercises().isEmpty()) continue;

                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayWorkout.getDay().toUpperCase());
                LocalDate sessionDate = referenceDate.with(TemporalAdjusters.nextOrSame(dayOfWeek));

                if (sessionDate.isBefore(today)) continue;

                WorkoutSession session = new WorkoutSession(program, sessionDate);
                WorkoutSession savedSession = workoutSessionRepository.save(session);

                for (ExerciseProgressDto epDto : dayWorkout.getExercises()) {
                    Exercise exercise = exerciseRepository.findById(epDto.getExerciseId())
                            .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

                    double weight = "lbs".equals(units) ? epDto.getWeight() * 0.453592 : epDto.getWeight();

                    ExerciseProgress progress = new ExerciseProgress(
                            savedSession,
                            exercise,
                            program.getUser(),
                            epDto.getReps(),
                            epDto.getSets(),
                            weight,
                            sessionDate
                    );
                    exerciseProgressRepository.save(progress);
                }
            }
        }
    }
    public void removeProgram(Long programId) {
        programRepository.deleteById(programId);
    }

    private Program getById(Long id) {
        return programRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid ID"));
    }
}