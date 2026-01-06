package com.example.fitplanner.config;

import com.example.fitplanner.dto.CreatedProgramDto;
import com.example.fitplanner.dto.ExerciseProgressDto;
import com.example.fitplanner.dto.DayWorkout;

import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.entity.model.WorkoutSession;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomModelMapper extends ModelMapper {

    public CustomModelMapper() {
        super();
        configureMapper();
    }

    private void configureMapper() {
        this.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        this.typeMap(ExerciseProgress.class, ExerciseProgressDto.class).addMappings(m -> {
            m.skip(ExerciseProgressDto::setExerciseId);
            m.map(src -> src.getExercise().getName(), ExerciseProgressDto::setName);
            m.map(src -> src.getExercise().getId(), ExerciseProgressDto::setExerciseId);
            m.map(src -> src.getWorkoutSession().getProgram().getName(), ExerciseProgressDto::setProgramName);
        });

        Converter<Set<WorkoutSession>, List<DayWorkout>> sessionsToDaysConverter = context -> {
            Map<String, List<ExerciseProgressDto>> fullWeek = new LinkedHashMap<>();

            for (DayOfWeek day : DayOfWeek.values()) {
                fullWeek.put(day.name(), new ArrayList<>());
            }

            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = startOfWeek.plusDays(7);

            if (context.getSource() != null) {
                context.getSource().stream()
                        .filter(d -> d.getScheduledFor() != null &&
                                d.getScheduledFor().isAfter(startOfWeek.minusDays(1)) &&
                                d.getScheduledFor().isBefore(endOfWeek))
                        .forEach(session -> {
                            String dayName = session.getScheduledFor().getDayOfWeek().name();
                            List<ExerciseProgressDto> dtos = session.getExercises().stream()
                                    .map(ex -> this.map(ex, ExerciseProgressDto.class)) // Use 'this' to map
                                    .collect(Collectors.toList());
                            fullWeek.get(dayName).addAll(dtos);
                        });
            }

            return fullWeek.entrySet().stream()
                    .map(entry -> new DayWorkout(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        };

        this.typeMap(Program.class, CreatedProgramDto.class).addMappings(m -> {
            m.using(sessionsToDaysConverter).map(Program::getSessions, CreatedProgramDto::setWeekDays);
        });
    }
}