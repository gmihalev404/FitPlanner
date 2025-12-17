package com.example.fitplanner.util;

import com.example.fitplanner.entity.enums.*;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.repository.ExerciseRepository;
import com.example.fitplanner.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final SHA256Hasher hasher;

    public DataInitializer(UserRepository userRepository, ExerciseRepository exerciseRepository,
                           SHA256Hasher hasher) {
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
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
            Category[] categories = Category.values();
            ExerciseType[] types = ExerciseType.values();
            EquipmentType[] equipments = EquipmentType.values();

            for (int i = 1; i <= 60; i++) {
                exercises.add(new Exercise(
                        "Exercise " + i,
                        categories[i % categories.length],
                        types[i % types.length],
                        equipments[i % equipments.length],
                        "icons/bench-press by Leremy from Flaticon.png",
                        "icons/bench-press by Leremy from Flaticon.png"
                ));
            }
            exerciseRepository.saveAll(exercises);
            System.out.println("Demo exercises initialized.");
        }
    }
}
