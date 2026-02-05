package com.example.fitplanner.service;

import com.example.fitplanner.dto.ExerciseSessionDto;
import com.example.fitplanner.dto.ExerciseSessionResultDto;
import com.example.fitplanner.entity.model.ExerciseProgress;
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

    @Transactional
    public void handleSuggestedChangeDecision(List<ExerciseSessionDto> exercises, Long exerciseId, boolean accepted) {
        for (ExerciseSessionDto ex : exercises) {
            if (ex.getExerciseId().equals(exerciseId) && ex.getSuggestedIncrease() != null) {
                ex.setIncreaseAccepted(accepted);
                ex.setSuggestedIncrease(null); // remove suggestion

                // ✅ Use progressId here
                ExerciseProgress progress = exerciseProgressRepository.findById(ex.getProgressId()).orElse(null);
                if (progress != null) {
                    if (accepted && ex.getSuggestedChange() != null) {
                        double newWeight = progress.getWeight() * (ex.getSuggestedChange() > 0 ? 1.05 : 0.95);
                        progress.setWeight(newWeight);
                    }
                    progress.resetCountersAfterSuggestion();
                    exerciseProgressRepository.save(progress);
                }
                break;
            }
        }
    }

    public Boolean getSuggestedIncreaseForExercise(Long userId, Long exerciseId, LocalDate date) {
        return exerciseProgressRepository.findByUserIdAndExerciseIdAndLastScheduled(userId, exerciseId, date)
                .map(ExerciseProgress::getSuggestedChangeIncrease)
                .orElse(null);
    }

    public Double getSuggestedChangeForExercise(Long userId, Long exerciseId, LocalDate date) {
        return exerciseProgressRepository.findByUserIdAndExerciseIdAndLastScheduled(userId, exerciseId, date)
                .map(ExerciseProgress::getSuggestedChange)
                .orElse(null);
    }

    public boolean isSessionFinished(Long userId, LocalDate date) {
        return workoutSessionRepository.isSessionFinished(userId, date);
    }

    public int getSetsCompletedForExercise(Long userId, Long exerciseId, LocalDate date) {
        Optional<ExerciseProgress> epOpt = exerciseProgressRepository
                .findByUserIdAndExerciseIdAndLastScheduled(userId, exerciseId, date);

        if (epOpt.isPresent()) {
            ExerciseProgress ep = epOpt.get();
            System.out.println("Found ExerciseProgress ID=" + ep.getId() +
                    ", setsCompleted=" + ep.getSetsCompleted() +
                    ", sets=" + ep.getSets() +
                    ", completed=" + ep.getCompleted() +
                    ", lastScheduled=" + ep.getLastScheduled() +
                    ", exerciseId=" + ep.getExercise().getId());
            return ep.getSetsCompleted();
        } else {
            System.out.println("No ExerciseProgress found for userId=" + userId +
                    ", exerciseId=" + exerciseId + ", date=" + date);
            return 0;
        }
    }

    @Transactional
    public void finishSession(List<ExerciseSessionResultDto> results, LocalDate date, Long userId) {
        // 1. Fetch the actual session entity for this user and date
        WorkoutSession session = workoutSessionRepository.findByUserIdAndScheduledFor(userId, date)
                .orElseThrow(() -> new RuntimeException("Workout session not found for " + date));

        // 2. Update individual exercise progress from the form data
        if (results != null) {
            for (ExerciseSessionResultDto result : results) {
                // The result.getExerciseId() here should be the PROGRESS ID from the hidden input
                exerciseProgressRepository.findById(result.getExerciseId()).ifPresent(ep -> {
                    ep.setSetsCompleted(result.getSetsCompleted());
                    ep.setCompleted(result.isFinished());
                    exerciseProgressRepository.save(ep);
                });
            }
        }

        // 3. Mark the whole session as finished and save
        session.setFinished(true);
        workoutSessionRepository.save(session);
    }
}