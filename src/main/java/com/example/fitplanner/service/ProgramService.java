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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

//    public Program getCurrentProgram() {
//        return programRepository.findFirstByOrderByCreatedAtDesc().orElse(null);
//    }

    public Program getProgramById(Long id) {
        return programRepository.findById(id).orElse(null);
    }

    public Program saveProgram(Program program) {
        return programRepository.save(program);
    }

    public void deleteProgram(Long id) {
        programRepository.deleteById(id);
    }

    public void createProgram(CreatedProgramDto dto, UserDto userDto, boolean isInLbUnits) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + userDto.getId()));
        Program program = new Program(dto.getName(), user);
        program = programRepository.save(program);
        LocalDate today = LocalDate.now();
        int totalWeeks;
        if (!dto.getRepeats() || dto.getScheduleMonths() == 0) totalWeeks = 1;
        else totalWeeks = dto.getScheduleMonths() * 4;
        for (int weekOffset = 0; weekOffset < totalWeeks; weekOffset++) {
            LocalDate targetWeekStart = today.plusWeeks(weekOffset);
            for (DayWorkout dayWorkout : dto.getWeekDays()) {
                if (dayWorkout.getExercises() == null || dayWorkout.getExercises().isEmpty()) continue;
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayWorkout.getDay().toUpperCase());
                LocalDate sessionDate = targetWeekStart.with(dayOfWeek);
                if (!dto.getRepeats() && sessionDate.isBefore(today)) {
                    sessionDate = sessionDate.plusWeeks(1);
                } else if (dto.getRepeats() && sessionDate.isBefore(today)) {
                    sessionDate = today.with(dayOfWeek);
                    if (sessionDate.isBefore(today)) {
                        sessionDate = sessionDate.plusWeeks(1);
                    }
                }
                WorkoutSession session = new WorkoutSession(program, sessionDate);
                program.addSession(session);
                workoutSessionRepository.save(session);
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
                            isInLbUnits ? epDto.getWeight() * LB_TO_KG : epDto.getWeight(),
                            sessionDate
                    );
                    session.addExercise(progress);
                    exerciseProgressRepository.save(progress);
                }
            }
        }
    }
}