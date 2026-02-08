package com.example.fitplanner.util;

import com.example.fitplanner.entity.enums.Difficulty;
import com.example.fitplanner.entity.enums.Gender;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.*;
import com.example.fitplanner.repository.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("dev")
public class DevDataInitializer {

    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ExerciseProgressRepository exerciseProgressRepository;
    private final NotificationRepository notificationRepository;
    private final SHA256Hasher hasher;

    public DevDataInitializer(UserRepository userRepository,
                              ProgramRepository programRepository,
                              ExerciseRepository exerciseRepository,
                              WorkoutSessionRepository workoutSessionRepository,
                              ExerciseProgressRepository exerciseProgressRepository,
                              NotificationRepository notificationRepository,
                              SHA256Hasher hasher) {
        this.userRepository = userRepository;
        this.programRepository = programRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.notificationRepository = notificationRepository;
        this.hasher = hasher;
    }

    @PostConstruct
    public void init() {
        User user = initDemoUser();
        initTrainerPrograms(); // This populates the Recommended section
        Program program = initProgram(user);
        List<Exercise> exercises = initExercises();
        initProgress(program, user, exercises);
    }

    // ----------------------------- Trainer Programs (For Home Page) -----------------------------

    private void initTrainerPrograms() {
        // Prevent duplicates: Only run if no trainer programs exist
        if (userRepository.existsByRole(Role.TRAINER)) return;

        // 1. Create a Professional Trainer
        User trainer = new User();
        trainer.setFirstName("Marcus");
        trainer.setLastName("Steel");
        trainer.setUsername("pro_coach");
        trainer.setEmail("coach@fitplanner.com");
        trainer.setRole(Role.TRAINER); // Required for your Home Page query
        trainer.setGender(Gender.MALE);
        trainer.setAge(34);
        trainer.setWeight(92.0);
        trainer.setExperience(Difficulty.ADVANCED);
        trainer.setPassword(hasher.hash("coach123"));
        userRepository.save(trainer);

        // 2. Create Public Programs with High Ratings
        String[] titles = {"Iron Foundation", "Hypertrophy V2", "Explosive Power", "Elite Shred"};
        String[] images = {
                "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=500",
                "https://images.unsplash.com/photo-1581009146145-b5ef03a7403f?w=500",
                "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=500",
                "https://images.unsplash.com/photo-1526506118085-60ce8714f8c5?w=500"
        };
        double[] ratings = {4.9, 4.8, 4.7, 4.5};
        Difficulty[] diffs = {Difficulty.BEGINNER, Difficulty.ADVANCED, Difficulty.INTERMEDIATE, Difficulty.ADVANCED};

        for (int i = 0; i < titles.length; i++) {
            Program p = new Program();
            p.setName(titles[i]);
            p.setUser(trainer); // Owned by Trainer
            p.setIsPublic(true); // Visible to everyone
            p.setDifficulty(diffs[i]);
            p.setRating(ratings[i]);
            p.setImageUrl(images[i]);
            p.setDescription("A professional " + titles[i] + " routine focusing on optimized performance and consistent progression markers.");
            p.setCreatedAt(java.time.LocalDateTime.now());
            p.setLastChanged(java.time.LocalDateTime.now());
            programRepository.save(p);
        }
        System.out.println("Trainer programs for recommendation initialized.");
    }

    // ----------------------------- Demo User -----------------------------

    private User initDemoUser() {
        return userRepository.findByUsername("testuser")
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername("testuser");
                    user.setEmail("testuser@test.com");
                    user.setRole(Role.CLIENT);
                    user.setLanguage("en");
                    user.setPassword(hasher.hash("test123"));

                    user.setFirstName("Test");
                    user.setLastName("User");
                    user.setGender(Gender.MALE);
                    user.setAge(25);
                    user.setWeight(70.0);
                    user.setExperience(Difficulty.BEGINNER);

                    LocalDate today = LocalDate.now();

                    // Add weight history
                    user.setWeight(70.0, today);
                    user.setWeight(69.5, today.minusWeeks(1));
                    user.setWeight(70.2, today.minusWeeks(2));
                    user.setWeight(69.0, today.minusWeeks(3));

                    // Add notifications
                    for (int i = 0; i < 10; i++) {
                        Notification n = new Notification();
                        n.setName("Update " + i);
                        n.setDescription("Your progress is looking great!");
                        n.setChecked((i % 2) == 0);
                        n.setObserver(user);
                        user.getNotifications().add(n);
                    }

                    return userRepository.save(user);
                });
    }

    // ----------------------------- Demo Client Program -----------------------------

    private Program initProgram(User user) {
        return programRepository.getByUserId(user.getId())
                .stream().findFirst()
                .orElseGet(() -> {
                    Program program = new Program();
                    program.setUser(user);
                    program.setName("My Personal Plan");
                    program.setIsPublic(false);
                    program.setDifficulty(user.getExperience());
                    return programRepository.save(program);
                });
    }

    // ----------------------------- Exercises -----------------------------

    private List<Exercise> initExercises() {
        if (exerciseRepository.count() >= 60) {
            return exerciseRepository.findAll();
        }
        List<Exercise> exercises = new ArrayList<>();
        for (int i = 1; i <= 60; i++) {
            Exercise exercise = new Exercise();
            exercise.setName("Exercise " + i);
            exercises.add(exercise);
        }
        exerciseRepository.saveAll(exercises);
        return exerciseRepository.findAll();
    }

    // ----------------------------- Progress Logic -----------------------------

    private void initProgress(Program program, User user, List<Exercise> exercises) {
        if (!exerciseProgressRepository.findByUserId(user.getId()).isEmpty()) return;

        Random random = new Random();
        LocalDate startDate = LocalDate.now().minusWeeks(12);

        for (int week = 0; week < 8; week++) {
            LocalDate sessionDate = startDate.plusWeeks(week);

            WorkoutSession session = new WorkoutSession();
            session.setProgram(program);
            session.setUser(user);
            session.setScheduledFor(sessionDate);
            session.setFinished(true); // Mark past sessions as done
            WorkoutSession savedSession = workoutSessionRepository.saveAndFlush(session);

            for (int i = 0; i < 5; i++) { // Add 5 exercises per session
                Exercise exercise = exercises.get(random.nextInt(exercises.size()));
                ExerciseProgress progress = new ExerciseProgress(
                        savedSession,
                        exercise,
                        user,
                        8 + random.nextInt(4),
                        3,
                        40.0 + random.nextInt(40),
                        sessionDate
                );
                progress.markCompleted(sessionDate);
                exerciseProgressRepository.save(progress);
            }
        }
    }
}