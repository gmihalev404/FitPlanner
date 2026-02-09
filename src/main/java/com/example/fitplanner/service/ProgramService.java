package com.example.fitplanner.service;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.model.*;
import com.example.fitplanner.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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

    // Inside ProgramService
    @Transactional(readOnly = true)
    public List<ProgramDto> getProgramsByUserId(Long userId) {
        List<Program> entities = programRepository.findAllByUserId(userId);

        return entities.stream().map(entity -> {
            // Force Hibernate to load the sessions collection
            Hibernate.initialize(entity.getSessions());

            ProgramDto dto = modelMapper.map(entity, ProgramDto.class);

            // Explicitly map the dates from Sessions to DateWorkout
            List<DateWorkout> dateWorkouts = entity.getSessions().stream()
                    .map(session -> {
                        DateWorkout dw = new DateWorkout();
                        dw.setDate(session.getScheduledFor());
                        return dw;
                    })
                    .sorted(Comparator.comparing(DateWorkout::getDate)) // Optional: keeps dates in order
                    .collect(Collectors.toList());

            dto.setWorkouts(dateWorkouts);
            return dto;
        }).collect(Collectors.toList());
    }

    public void createProgram(CreatedProgramDto dto, ProgramsUserDto userDto, String units, Difficulty difficulty) {
        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Program program = new Program(dto.getName(), user, dto.getScheduleMonths(), dto.getNotifications(), dto.getIsPublic(), dto.getImageUrl());
        program.setDifficulty(difficulty);
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
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            program.setImageUrl(dto.getImageUrl());
        }
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
        // Use the Monday of the current week as the anchor for generation
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

        // Calculate how many weeks to generate (Default to 1 if not repeating)
        int totalWeeks = (!dto.getRepeats() || dto.getScheduleMonths() == 0) ? 1 : dto.getScheduleMonths() * 4;

        for (int weekOffset = 0; weekOffset < totalWeeks; weekOffset++) {
            LocalDate targetWeekStart = startOfWeek.plusWeeks(weekOffset);

            for (DayWorkout dayWorkout : dto.getWeekDays()) {
                // Skip days with no exercises defined
                if (dayWorkout.getExercises() == null || dayWorkout.getExercises().isEmpty()) continue;

                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dayWorkout.getDay().toUpperCase());
                LocalDate sessionDate = targetWeekStart.with(dayOfWeek);

                // Logic to handle dates that have already passed
                if (sessionDate.isBefore(today)) {
                    if (dto.getRepeats()) {
                        continue; // Skip past days for repeating programs
                    } else {
                        sessionDate = sessionDate.plusWeeks(1); // Push one-time sessions to next week
                    }
                }

                final LocalDate finalSessionDate = sessionDate;

                // --- THE FIX ---
                // 1. We look for a session that matches USER + DATE + PROGRAM.
                // This ensures Program B doesn't find Program A's sessions.
                WorkoutSession session = workoutSessionRepository
                        .findSessionByDateAndProgram(user.getId(), finalSessionDate, program.getId())
                        .orElseGet(() -> {
                            // 2. If no session exists for THIS program on THIS day, create it.
                            WorkoutSession newSession = new WorkoutSession();
                            newSession.setProgram(program);
                            newSession.setUser(user);
                            newSession.setScheduledFor(finalSessionDate);
                            newSession.setFinished(false);
                            return workoutSessionRepository.save(newSession);
                        });

                // Note: We removed the "stealing" logic block (session.setProgram)
                // because the find method above is now program-specific.

                for (ExerciseProgressDto epDto : dayWorkout.getExercises()) {
                    Exercise exercise = exerciseRepository.findById(epDto.getExerciseId())
                            .orElseThrow(() -> new IllegalArgumentException("Exercise not found"));

                    // 3. Avoid duplicate exercises within the same session
                    boolean exists = session.getExercises().stream()
                            .anyMatch(ep -> ep.getExercise().getId().equals(epDto.getExerciseId()));

                    if (!exists) {
                        // Convert weight if user is using imperial units
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

    @Transactional
    public void forkProgram(Long programId, Long userId) {
        Program original = programRepository.findById(programId).orElseThrow();
        User newUser = userRepository.findById(userId).orElseThrow();
        if(original.getUser().getId().equals(userId)){
            return;
        }
        Program fork = new Program();
        fork.setUser(newUser);
        fork.setName(original.getName() + " (Copied)");
        fork.setCreatedAt(LocalDateTime.now());
        fork.setLastChanged(LocalDateTime.now());
        // Copy other essential fields
        fork.setDifficulty(original.getDifficulty());

        // 1. Save Program first
        Program savedFork = programRepository.save(fork);

        // 2. Initialize the sessions list to avoid nulls
        if (savedFork.getSessions() == null) {
            savedFork.setSessions(new LinkedList<>());
        }

        LocalDate originalStart = original.getSessions().stream()
                .map(WorkoutSession::getScheduledFor)
                .min(LocalDate::compareTo).orElse(LocalDate.now());
        long dayOffset = java.time.temporal.ChronoUnit.DAYS.between(originalStart, LocalDate.now());

        for (WorkoutSession originalSession : original.getSessions()) {
            LocalDate newDate = originalSession.getScheduledFor().plusDays(dayOffset);

            WorkoutSession targetSession = workoutSessionRepository
                    .findByUserIdAndScheduledFor(newUser.getId(), newDate)
                    .orElseGet(() -> {
                        WorkoutSession s = new WorkoutSession();
                        s.setUser(newUser);
                        s.setProgram(savedFork); // Link to new program
                        s.setScheduledFor(newDate);
                        s.setFinished(false);
                        return workoutSessionRepository.save(s);
                    });

            // CRITICAL: Ensure the bidirectional link is established
            targetSession.setProgram(savedFork);
            savedFork.getSessions().add(targetSession);

            for (ExerciseProgress originalEp : originalSession.getExercises()) {
                ExerciseProgress newEp = new ExerciseProgress();
                newEp.setWorkoutSession(targetSession);
                newEp.setExercise(originalEp.getExercise());
                newEp.setUser(newUser);
                newEp.setReps(originalEp.getReps());
                newEp.setSets(originalEp.getSets());
                newEp.setWeight(originalEp.getWeight());
                newEp.setLastScheduled(newDate);
                exerciseProgressRepository.save(newEp);
            }
        }
        // Final save/flush to sync the relationship
        programRepository.saveAndFlush(savedFork);
    }
    public List<ForkableProgramDto> getRecommendedPrograms(Long currentUserId) {
        // Fetch top 4 trainer programs that the user doesn't own
        Pageable limit = PageRequest.of(0, 4);
        return programRepository.findTopRatedTrainerPrograms(currentUserId, limit)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ForkableProgramDto mapToDto(Program program) {
        ForkableProgramDto dto = new ForkableProgramDto();
        dto.setId(program.getId());
        dto.setName(program.getName());

        // Handle potentially long descriptions for the UI
        String desc = program.getDescription();
        if (desc != null && desc.length() > 80) {
            desc = desc.substring(0, 77) + "...";
        }
        dto.setDescriptionShort(desc);

        dto.setImageUrl(program.getImageUrl());

        // Convert Enums to Strings for the template
        dto.setDifficulty(program.getDifficulty() != null ?
                program.getDifficulty().name() : "INTERMEDIATE");

        dto.setRating(program.getRating() != null ? program.getRating() : 0.0);

        return dto;
    }

    public List<ForkableProgramDto> searchProgramsAndMap(String query) {
        List<Program> entities = (query == null || query.isBlank())
                ? programRepository.findAllByIsPublicTrue()
                : programRepository.searchPublicPrograms(query);

        return entities.stream()
                .map(this::mapToDto) // Uses your existing private mapToDto method
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgramDetailsDto getProgramDetails(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow();

        ProgramDetailsDto dto = new ProgramDetailsDto();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setDescription(program.getDescription());
        dto.setImageUrl(program.getImageUrl());
        dto.setDifficulty(program.getDifficulty().name());
        dto.setRating(program.getRating());
        dto.setTrainerName(program.getUser().getFirstName() + " " + program.getUser().getLastName());

        // 1. Намираме понеделник на текущата седмица за база
        LocalDate startOfWeek = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        // 2. Группираме съществуващите упражнения по ден от седмицата
        Map<java.time.DayOfWeek, List<ExerciseProgressDto>> exerciseMap = new java.util.EnumMap<>(java.time.DayOfWeek.class);
        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) exerciseMap.put(d, new ArrayList<>());

        program.getSessions().forEach(session -> {
            java.time.DayOfWeek dayOfWeek = session.getScheduledFor().getDayOfWeek();
            if (exerciseMap.get(dayOfWeek).isEmpty()) {
                List<ExerciseProgressDto> dtos = session.getExercises().stream()
                        .map(ep -> {
                            ExerciseProgressDto exDto = new ExerciseProgressDto(
                                    ep.getExercise().getId(), ep.getExercise().getName(),
                                    ep.getReps(), ep.getSets(), ep.getWeight()
                            );
                            exDto.setFinished(ep.getCompleted());
                            return exDto;
                        }).collect(Collectors.toList());
                exerciseMap.put(dayOfWeek, dtos);
            }
        });

        // 3. Пълним списъка с DateWorkout
        List<DateWorkout> workouts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            DateWorkout dw = new DateWorkout(date, exerciseMap.get(date.getDayOfWeek()));
            dw.setProgramName(program.getName());
            workouts.add(dw);
        }

        dto.setWorkouts(workouts);
        return dto;
    }    @Transactional
    public void addRating(Long id, int stars) {
        Program program = programRepository.findById(id).orElseThrow();
        program.addStars(stars);
    }

    public List<DayWorkout> getProgramTemplate(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow();

        // Подготвяме празна карта за всички 7 дни, за да гарантираме реда и наличието на Rest Days
        Map<DayOfWeek, List<ExerciseProgressDto>> weeklyPlan = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            weeklyPlan.put(day, new ArrayList<>());
        }

        // Вземаме сесиите. Ако програмата е нова, вземаме първите записи.
        // Групираме упражненията според деня от седмицата на датата 'scheduledFor'
        program.getSessions().stream()
                .forEach(session -> {
                    DayOfWeek dayOfWeek = session.getScheduledFor().getDayOfWeek();

                    // Мапваме ExerciseProgress към ExerciseProgressDto
                    List<ExerciseProgressDto> dtos = session.getExercises().stream()
                            .map(ep -> {
                                ExerciseProgressDto dto = new ExerciseProgressDto(
                                        ep.getExercise().getId(),
                                        ep.getExercise().getName(),
                                        ep.getReps(),
                                        ep.getSets(),
                                        ep.getWeight()
                                );
                                dto.setProgramName(program.getName());
                                return dto;
                            })
                            .collect(Collectors.toList());

                    // Добавяме упражненията към съответния ден (ако има повече сесии в един и същи ден от седмицата, вземаме първата)
                    if (weeklyPlan.get(dayOfWeek).isEmpty()) {
                        weeklyPlan.put(dayOfWeek, dtos);
                    }
                });

        // Превръщаме Map-а в списък от DayWorkout, подреден от Понеделник
        return weeklyPlan.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    // Превръщаме DayOfWeek (MONDAY) в красив String (Monday)
                    String dayName = entry.getKey().name().charAt(0) + entry.getKey().name().substring(1).toLowerCase();
                    return new DayWorkout(dayName, entry.getValue());
                })
                .collect(Collectors.toList());
    }
}