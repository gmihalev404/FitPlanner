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
import java.time.LocalDateTime;
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
    private final QuoteRepository quoteRepository;
    private final MilestoneRepository milestoneRepository;
    private final SHA256Hasher hasher;
    private final Random random = new Random();

    public DevDataInitializer(UserRepository userRepository,
                              ProgramRepository programRepository,
                              ExerciseRepository exerciseRepository,
                              WorkoutSessionRepository workoutSessionRepository,
                              ExerciseProgressRepository exerciseProgressRepository,
                              QuoteRepository quoteRepository,
                              MilestoneRepository milestoneRepository,
                              SHA256Hasher hasher) {
        this.userRepository = userRepository;
        this.programRepository = programRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.quoteRepository = quoteRepository;
        this.milestoneRepository = milestoneRepository;
        this.hasher = hasher;
    }

    @PostConstruct
    public void init() {
        initQuotes();
        User user = initDemoUser();
        initTrainerPrograms();
        Program program = initProgram(user);
        List<Exercise> exercises = initExercises();
        initProgressAndMilestones(program, user, exercises);
    }

    private void initQuotes() {
        if (quoteRepository.count() > 0) return;
        quoteRepository.saveAll(List.of(
                new Quote("The only bad workout is the one that didn't happen.", "Unknown"),
                new Quote("Action is the foundational key to all success.", "Pablo Picasso"),
                new Quote("Discipline is doing what needs to be done.", "Unknown")
        ));
    }

    private User initDemoUser() {
        User user = userRepository.findByUsername("testuser").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("testuser");
            newUser.setEmail("testuser@test.com");
            newUser.setRole(Role.CLIENT);
            newUser.setPassword(hasher.hash("test123"));
            newUser.setFirstName("Test");
            newUser.setLastName("User");
            newUser.setGender(Gender.MALE);
            newUser.setAge(25);
            newUser.setWeight(85.0);
            newUser.setExperience(Difficulty.BEGINNER);
            return newUser;
        });

        // ---------------------------------------------------------
        // DENSE WEIGHT HISTORY (Daily for 30 days)
        // ---------------------------------------------------------
        List<WeightEntry> weightHistory = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 30; i >= 0; i--) {
            // Trend: Start at 88kg 30 days ago, end at ~84kg today
            double trendWeight = 84.0 + (i * 0.13);
            // Add "Noise": Small daily fluctuations (+/- 0.3kg) for realism
            double noise = (random.nextDouble() - 0.5) * 0.6;

            weightHistory.add(new WeightEntry(trendWeight + noise, today.minusDays(i)));
        }

        user.setWeightChanges(weightHistory);
        user.setStreak(14);
        user.setLastWorkoutDate(today);
        user.setTheme("dark");
        user.setLanguage("en");

        return userRepository.saveAndFlush(user);
    }

    private void initTrainerPrograms() {
        // Only initialize if we don't have trainers yet
        if (userRepository.findAll().stream().anyMatch(u -> u.getRole() == Role.TRAINER)) return;

        // 1. Strength Coach - Focused on Heavy Lifting
        User strengthCoach = new User(
                "Marcus", "Vane", "strength_master",
                Role.TRAINER, Gender.MALE, 38, 105.0,
                Difficulty.ADVANCED, "marcus@fitplanner.com", hasher.hash("coach123")
        );
        userRepository.save(strengthCoach);

        Program powerProgram = new Program();
        powerProgram.setName("Powerlifting Basics");
        powerProgram.setUser(strengthCoach);
        powerProgram.setIsPublic(true);
        powerProgram.setDifficulty(Difficulty.ADVANCED);
        programRepository.save(powerProgram);

        // 2. Bodyweight Expert - Focused on Calisthenics
        User caliExpert = new User(
                "Elena", "Rodriguez", "elena_fit",
                Role.TRAINER, Gender.FEMALE, 29, 62.0,
                Difficulty.INTERMEDIATE, "elena@fitplanner.com", hasher.hash("coach123")
        );
        userRepository.save(caliExpert);

        Program caliProgram = new Program();
        caliProgram.setName("Bodyweight Mastery");
        caliProgram.setUser(caliExpert);
        caliProgram.setIsPublic(true);
        caliProgram.setDifficulty(Difficulty.INTERMEDIATE);
        programRepository.save(caliProgram);

        // 3. General Fitness Trainer - Focused on Hypertrophy
        User hypertrophyCoach = new User(
                "Sarah", "Chen", "sarah_gains",
                Role.TRAINER, Gender.FEMALE, 31, 68.0,
                Difficulty.ADVANCED, "sarah@fitplanner.com", hasher.hash("coach123")
        );
        userRepository.save(hypertrophyCoach);

        Program massProgram = new Program();
        massProgram.setName("Hypertrophy 101");
        massProgram.setUser(hypertrophyCoach);
        massProgram.setIsPublic(true);
        massProgram.setDifficulty(Difficulty.BEGINNER);
        programRepository.save(massProgram);

        System.out.println("Trainers and Public Programs initialized.");
    }
    private Program initProgram(User user) {
        return programRepository.getByUserId(user.getId()).stream().findFirst().orElseGet(() -> {
            Program p = new Program();
            p.setUser(user);
            p.setName("My Personal Plan");
            return programRepository.save(p);
        });
    }

    private List<Exercise> initExercises() {
        if (exerciseRepository.count() >= 5) return exerciseRepository.findAll();
        String[] names = {"Bench Press", "Squat", "Deadlift", "Pullups", "Rows"};
        for (String name : names) {
            Exercise e = new Exercise();
            e.setName(name);
            exerciseRepository.save(e);
        }
        return exerciseRepository.findAll();
    }

    private void initProgressAndMilestones(Program program, User user, List<Exercise> exercises) {
        // Clear all previous dev records to avoid duplicates and outdated charts
        exerciseProgressRepository.deleteAll(exerciseProgressRepository.findByUserId(user.getId()));
        workoutSessionRepository.deleteAll(workoutSessionRepository.findByUserId(user.getId()));
        workoutSessionRepository.flush();

        LocalDate today = LocalDate.now();

        // ---------------------------------------------------------
        // DENSE EXERCISE HISTORY (Every 2 days for 30 days)
        // ---------------------------------------------------------
        for (int i = 30; i >= 0; i -= 2) {
            LocalDate sessionDate = today.minusDays(i);

            WorkoutSession session = new WorkoutSession();
            session.setProgram(program);
            session.setUser(user);
            session.setScheduledFor(sessionDate);
            session.setFinished(true);
            WorkoutSession savedSession = workoutSessionRepository.saveAndFlush(session);

            for (Exercise ex : exercises) {
                ExerciseProgress ep = new ExerciseProgress();
                ep.setWorkoutSession(savedSession);
                ep.setExercise(ex);
                ep.setUser(user);

                // Progressive Overload Logic:
                // As 'i' gets smaller (closer to today), weight increases
                double baseWeight = 50.0;
                double weightGain = (30 - i) * 0.8; // Gains 0.8kg per session
                double fluctuation = random.nextInt(3); // Small random variation

                ep.setWeight(baseWeight + weightGain + fluctuation);
                ep.setReps(10);
                ep.setSets(3);
                ep.setLastScheduled(sessionDate);
                ep.setCompleted(true);
                ep.setLastCompleted(sessionDate);
                ep.setSetsCompleted(3);

                exerciseProgressRepository.save(ep);
            }
        }

        // Expanded Milestones for Activity Feed
        String[] achievementTitles = {
                "First Step", "7 Day Streak", "Power Lifter",
                "Consistency King", "Volume Warrior", "Century Club"
        };
        for (int i = 0; i < achievementTitles.length; i++) {
            Milestone m = new Milestone();
            m.setUser(user);
            m.setTitle(achievementTitles[i]);
            m.setAchievedAt(LocalDateTime.now().minusDays(i * 5));
            milestoneRepository.save(m);
        }

        exerciseProgressRepository.flush();
    }
}