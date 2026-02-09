package com.example.fitplanner.service;

import com.example.fitplanner.dto.DayWorkout;
import com.example.fitplanner.dto.ExerciseProgressDto;
import com.example.fitplanner.dto.ExerciseSessionDto;
import com.example.fitplanner.dto.ExerciseSessionResultDto;
import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.entity.model.WorkoutSession;
import com.example.fitplanner.repository.ExerciseProgressRepository;
import com.example.fitplanner.repository.UserRepository;
import com.example.fitplanner.repository.WorkoutSessionRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExerciseProgressRepository exerciseProgressRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final double KG_TO_LB = 2.20462262;

    public WorkoutSessionService(WorkoutSessionRepository workoutSessionRepository,
                                 ExerciseProgressRepository exerciseProgressRepository,
                                 UserRepository userRepository,
                                 ModelMapper modelMapper) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<ExerciseSessionDto> getWorkoutsByProgramIdsAndDate(List<Long> programIds,
                                                                   LocalDate date,
                                                                   String units,
                                                                   Long userId) {
        if (programIds == null || programIds.isEmpty()) return new ArrayList<>();

        List<WorkoutSession> sessions = workoutSessionRepository.getByProgramIdsAndDate(programIds, date);
        List<ExerciseSessionDto> exercises = new ArrayList<>();

        for (WorkoutSession session : sessions) {
            session.getExercises().forEach(progress -> {
                ExerciseSessionDto dto = new ExerciseSessionDto();
                dto.setProgressId(progress.getId());                 // ID of ExerciseProgress
                dto.setExerciseId(progress.getExercise().getId());  // ID of Exercise
                dto.setName(progress.getExercise().getName());
                dto.setProgramName(session.getProgram().getName());
                dto.setReps(progress.getReps());
                dto.setSets(progress.getSets());
                dto.setFinished(progress.getCompleted());
                dto.setSetsCompleted(progress.getSetsCompleted());
                dto.setSuggestedIncrease(progress.getSuggestedChangeIncrease());
                dto.setSuggestedChange(progress.getSuggestedChange());
                dto.setIncreaseAccepted(null);

                double weight = progress.getWeight();
                if ("lbs".equals(units)) weight = weight * KG_TO_LB;
                dto.setWeight(weight);

                exercises.add(dto);

                System.out.println("Mapped ExerciseSessionDto: progressId=" + progress.getId() +
                        ", exerciseId=" + progress.getExercise().getId() +
                        ", name=" + progress.getExercise().getName() +
                        ", sets=" + progress.getSets() +
                        ", reps=" + progress.getReps() +
                        ", weight=" + progress.getWeight() +
                        ", completed=" + progress.getCompleted());
            });
        }
        return exercises;
    }

    public Boolean getSuggestedIncreaseForExercise(Long userId, Long exerciseId, LocalDate date) {
        List<ExerciseProgress> progressList = exerciseProgressRepository
                .findByUserIdAndExerciseIdAndLastScheduled(userId, exerciseId, date);

        return progressList.isEmpty() ? null : progressList.get(0).getSuggestedChangeIncrease();
    }

    public Double getSuggestedChangeForExercise(Long userId, Long exerciseId, LocalDate date) {
        List<ExerciseProgress> progressList = exerciseProgressRepository
                .findByUserIdAndExerciseIdAndLastScheduled(userId, exerciseId, date);

        return progressList.isEmpty() ? null : progressList.get(0).getSuggestedChange();
    }

    public int getSetsCompletedForExercise(Long userId, Long exerciseId, LocalDate date) {
        List<ExerciseProgress> progressList = exerciseProgressRepository
                .findByUserIdAndExerciseIdAndLastScheduled(userId, exerciseId, date);

        if (!progressList.isEmpty()) {
            ExerciseProgress ep = progressList.get(0);
            System.out.println("Found ExerciseProgress ID=" + ep.getId() + " (First of " + progressList.size() + ")");
            return ep.getSetsCompleted();
        } else {
            System.out.println("No ExerciseProgress found for userId=" + userId + ", exerciseId=" + exerciseId + ", date=" + date);
            return 0;
        }
    }

    public Boolean isSessionFinished(Long userId, LocalDate date) {
        // Вземаме всички статуси (вече е List)
        List<Boolean> statuses = workoutSessionRepository.isSessionFinished(userId, date);

        // Ако няма записи за този ден, не е приключен
        if (statuses.isEmpty()) {
            return false;
        }

        // Връщаме TRUE само ако всяка една сесия в списъка е TRUE
        return statuses.stream().allMatch(status -> status != null && status);
    }


    @Transactional
    public void finishSession(List<ExerciseSessionResultDto> results, LocalDate date, Long userId) {
        // 1. Fetch ALL sessions for this user on this date (since one day can have multiple programs)
        List<WorkoutSession> sessions = workoutSessionRepository.findAllByUserIdAndScheduledForWithExercises(userId, date);

        if (sessions.isEmpty()) {
            throw new RuntimeException("No workout sessions found for this date");
        }

        if (results != null) {
            for (ExerciseSessionResultDto result : results) {
                // result.getExerciseId() is actually the progressId (PK of ExerciseProgress)
                exerciseProgressRepository.findById(result.getExerciseId()).ifPresent(ep -> {
                    ep.setSetsCompleted(result.getSetsCompleted());

                    // Use our new logic: completed only if ALL sets are done
                    boolean isSuccessful = (result.getSetsCompleted() >= ep.getSets());

                    if (isSuccessful) {
                        ep.markCompleted(date);
                    } else {
                        ep.markMissed(date);
                    }

                    // --- STREAK TRIGGERS ---
                    if (ep.getCompletedExercisesInARow() >= 8) {
                        ep.setSuggestedChangeIncrease(true);
                        ep.setSuggestedChange(5.0);
                    } else if (ep.getMissedExercisesInARow() >= 6) {
                        ep.setSuggestedChangeIncrease(false);
                        ep.setSuggestedChange(10.0);
                    } else {
                        ep.setSuggestedChangeIncrease(null);
                        ep.setSuggestedChange(0.0);
                    }

                    exerciseProgressRepository.save(ep);
                });
            }
        }

        // 2. Mark all sessions for this day as finished and update user streak
        for (WorkoutSession session : sessions) {
            boolean allExercisesInThisSessionCompleted = session.getExercises().stream()
                    .allMatch(ep -> ep.getCompleted() != null && ep.getCompleted());

            if (allExercisesInThisSessionCompleted && !session.isFinished()) {
                User user = userRepository.findById(userId).orElseThrow();
                user.setStreak(user.getStreak() + 1);
                userRepository.save(user);
            }

            session.setFinished(true);
            workoutSessionRepository.save(session);
        }
    }
    @Transactional
    public void handleSuggestedChangeDecision(List<ExerciseSessionDto> exercises, Long exerciseId, boolean accepted) {
        for (ExerciseSessionDto ex : exercises) {
            if (ex.getExerciseId().equals(exerciseId) && ex.getSuggestedIncrease() != null) {

                ExerciseProgress progress = exerciseProgressRepository.findById(ex.getProgressId()).orElse(null);
                if (progress != null) {
                    if (accepted) {
                        applyDoubleProgression(progress);
                    }
                    // Reset streaks regardless of acceptance
                    progress.resetCountersAfterSuggestion();
                    exerciseProgressRepository.save(progress);
                }
                break;
            }
        }
    }

    private void applyDoubleProgression(ExerciseProgress ep) {
        boolean isIncrease = Boolean.TRUE.equals(ep.getSuggestedChangeIncrease());
        double percent = (ep.getSuggestedChange() != null) ? ep.getSuggestedChange() / 100.0 : 0.05;

        int currentReps = ep.getReps();

        if (isIncrease) {
            // Logic: Weight increases at 10 reps or 30 reps. Otherwise, reps increase.
            if (currentReps == 10 || currentReps >= 30) {
                ep.setWeight(ep.getWeight() * (1 + percent));
                // Optional: reset reps back to 8 after a weight increase to allow room to grow
                if(currentReps >= 30) ep.setReps(10);
            } else {
                ep.setReps(currentReps + 1);
            }
        } else {
            // Logic: Weight decreases if at the 4-rep floor. Otherwise, reps decrease.
            if (currentReps <= 4) {
                ep.setWeight(ep.getWeight() * (1 - percent));
                ep.setReps(8); // Move back to a safer rep range
            } else {
                ep.setReps(currentReps - 1);
            }
        }
    }
}