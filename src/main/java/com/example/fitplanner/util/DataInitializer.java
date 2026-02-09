package com.example.fitplanner.util;

import com.example.fitplanner.entity.enums.*;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.ExerciseRepository;
import com.example.fitplanner.repository.ProgramRepository;
import com.example.fitplanner.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final ProgramRepository programRepository;
    private final SHA256Hasher hasher;

    public DataInitializer(UserRepository userRepository, ExerciseRepository exerciseRepository, ProgramRepository programRepository,
                           SHA256Hasher hasher) {
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.programRepository = programRepository;
        this.hasher = hasher;
    }

    @PostConstruct
    public void initData() {
        try {
            initAdmin();
            initDemoExercises();
        } catch (Exception e) {
            System.err.println("Failed to initialize demo data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initAdmin() {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User(
                    "Admin",
                    "Admin",
                    "admin",
                    Role.ADMIN,
                    Gender.MALE,
                    30,
                    80.0,
                    Difficulty.BEGINNER,
                    "admin@example.com",
                    hasher.hash("admin123")
            );
            userRepository.save(admin);
            System.out.println("Admin user created.");
        }
    }

    private void initDemoExercises() {
        if (exerciseRepository.count() == 0) {
            List<Exercise> exercises = new ArrayList<>();

            // Mapping your files to your specific Category, ExerciseType, and EquipmentType enums
            Object[][] exerciseData = {
                    {"Bench Press", Category.CHEST, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/bench-press by Leremy from Flaticon.png", null},
                    {"Deadlift", Category.BACK, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/deadlift By Iconjam from Flaticon.png", null},
                    {"Dips", Category.CHEST, ExerciseType.REPETITIONS, EquipmentType.BODY_WEIGHT, "/icons/dips by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Bicep Curls", Category.BICEPS, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/dumbbell-bicep-curl by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Lateral Raises", Category.CORE, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/lateral-raises by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Lunges", Category.LEGS, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/lunge.png", "/videos/squats.mp4"},
                    {"Pull-ups", Category.BACK, ExerciseType.REPETITIONS, EquipmentType.BODY_WEIGHT, "/icons/pull-ups by Ehtisham Abid from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Push-ups", Category.CHEST, ExerciseType.REPETITIONS, EquipmentType.BODY_WEIGHT, "/icons/pushups by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Rows", Category.BACK, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/rows by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Shoulder Press", Category.CHEST, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/shoulder-press by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Squats", Category.LEGS, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/squat by Leremy from Flaticon.png", "/videos/squats.mp4"},
                    {"Triceps Cable Extension", Category.TRICEPS, ExerciseType.REPETITIONS, EquipmentType.MACHINE, "/icons/triceps-cable-extention by Leremy from Flaticon.png", "/videos/push-ups.mp4"},
                    {"Triceps Overhead Extension", Category.TRICEPS, ExerciseType.REPETITIONS, EquipmentType.WEIGHTED, "/icons/triceps-overhead-extention by Leremy from Flaticon.png", "/videos/push-ups.mp4"}
            };

            for (Object[] data : exerciseData) {
                exercises.add(new Exercise(
                        (String) data[0],             // Name
                        (Category) data[1],           // Category
                        (ExerciseType) data[2],       // ExerciseType
                        (EquipmentType) data[3],      // EquipmentType
                        (String) data[4],             // Image URL
                        (String) data[5]              // Video URL
                ));
            }

            exerciseRepository.saveAll(exercises);
            System.out.println("Demo exercises successfully initialized with custom Enums.");
        }
    }}
