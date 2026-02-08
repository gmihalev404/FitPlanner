package com.example.fitplanner.config;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.model.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomModelMapper extends ModelMapper {

    private static final double KG_TO_LB = 2.20462262;

    public CustomModelMapper() {
        super();
        configureMapper();
    }

    private void configureMapper() {
        this.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        // --- ExerciseProgress -> ExerciseProgressDto ---
        this.typeMap(ExerciseProgress.class, ExerciseProgressDto.class).addMappings(m -> {
            m.skip(ExerciseProgressDto::setExerciseId);
            m.map(src -> src.getExercise().getName(), ExerciseProgressDto::setName);
            m.map(src -> src.getExercise().getId(), ExerciseProgressDto::setExerciseId);
            m.map(src -> src.getWorkoutSession().getProgram().getName(), ExerciseProgressDto::setProgramName);
        });

        // --- Program -> CreatedProgramDto ---
        // FIX: Use Collection<?> instead of Set<?> to be compatible with Hibernate's PersistentBag (List)
        Converter<Collection<WorkoutSession>, List<DayWorkout>> sessionsToWeekConverter = context -> {
            Map<String, List<ExerciseProgressDto>> weekMap = new LinkedHashMap<>();
            for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
                weekMap.put(day.name(), new ArrayList<>());
            }

            if (context.getSource() != null) {
                context.getSource().stream()
                        .filter(session -> session.getScheduledFor() != null)
                        .forEach(session -> {
                            String dayName = session.getScheduledFor().getDayOfWeek().name();
                            List<ExerciseProgressDto> exercisesForDay = weekMap.get(dayName);

                            // Optimize: Use a simple Set for lookup instead of re-streaming the DTO list every time
                            Set<Long> addedExerciseIds = exercisesForDay.stream()
                                    .map(ExerciseProgressDto::getExerciseId)
                                    .collect(Collectors.toSet());

                            if (session.getExercises() != null) {
                                session.getExercises().stream()
                                        .map(ex -> this.map(ex, ExerciseProgressDto.class))
                                        .filter(dto -> !addedExerciseIds.contains(dto.getExerciseId()))
                                        .forEach(exercisesForDay::add);
                            }
                        });
            }

            return weekMap.entrySet().stream()
                    .map(entry -> new DayWorkout(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        };

        this.createTypeMap(Program.class, CreatedProgramDto.class)
                .addMappings(m -> {
                    m.map(Program::getName, CreatedProgramDto::setName);
                    m.map(Program::getRepeats, CreatedProgramDto::setRepeats);
                    m.map(Program::getScheduleMonths, CreatedProgramDto::setScheduleMonths);
                    m.map(Program::getNotifications, CreatedProgramDto::setNotifications);
                    m.map(Program::getIsPublic, CreatedProgramDto::setIsPublic);
                    // This mapping now uses the Collection-based converter
                    m.using(sessionsToWeekConverter).map(Program::getSessions, CreatedProgramDto::setWeekDays);
                });

        // --- User -> StatsUserDto ---
        this.typeMap(User.class, StatsUserDto.class).setPostConverter(context -> {
            User source = context.getSource();
            StatsUserDto destination = context.getDestination();

            if (source == null || destination == null) return destination;

            boolean isLbs = "lbs".equalsIgnoreCase(source.getMeasuringUnits());

            if (source.getCompletedExercises() != null) {
                List<StatsExerciseDto> convertedProgress = getStatsExerciseDtos(source, isLbs, KG_TO_LB);
                destination.setProgresses(convertedProgress);
            }

            if (source.getWeightChanges() != null) {
                List<WeightEntryDto> weightDtos = new ArrayList<>();
                for (WeightEntry entry : source.getWeightChanges()) {
                    if (entry == null) continue;
                    WeightEntryDto dto = new WeightEntryDto();
                    Double weightVal = entry.getWeight();

                    if (isLbs && weightVal != null) {
                        weightVal = weightVal * KG_TO_LB;
                        weightVal = Math.round(weightVal * 100.0) / 100.0;
                    }

                    dto.setWeight(weightVal);
                    dto.setDate(entry.getDate());
                    weightDtos.add(dto);
                }
                destination.setWeightChanges(weightDtos);
            }

            destination.setMeasuringUnits(source.getMeasuringUnits());
            return destination;
        });
    }

    private List<StatsExerciseDto> getStatsExerciseDtos(User source, boolean isLbs, double KG_TO_LB) {
        List<StatsExerciseDto> convertedProgress = new ArrayList<>();
        if (source.getCompletedExercises() == null) return convertedProgress;

        for (ExerciseProgress entity : source.getCompletedExercises()) {
            try {
                if (entity.getExercise() == null) continue;

                StatsExerciseDto dto = new StatsExerciseDto();
                dto.setExerciseId(entity.getExercise().getId());
                dto.setWeight(entity.getWeight());
                dto.setCompletedExercisesInARow(entity.getCompletedExercisesInARow());

                LocalDate date = (entity.getLastCompleted() != null) ?
                        entity.getLastCompleted() : entity.getLastScheduled();
                dto.setCompletedDate(date);

                if (isLbs && dto.getWeight() != null) {
                    double lbVal = dto.getWeight() * KG_TO_LB;
                    dto.setWeight(Math.round(lbVal * 100.0) / 100.0);
                }

                if (dto.getCompletedDate() != null) {
                    convertedProgress.add(dto);
                }
            } catch (Exception ignored) {}
        }
        return convertedProgress;
    }
}