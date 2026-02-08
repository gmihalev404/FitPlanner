package com.example.fitplanner.service;

import com.example.fitplanner.dto.DashboardStatsDto;
import com.example.fitplanner.repository.ExerciseProgressRepository;
import com.example.fitplanner.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final ExerciseProgressRepository exerciseProgressRepository;

    public DashboardService(UserRepository userRepository, ExerciseProgressRepository exerciseProgressRepository) {
        this.userRepository = userRepository;
        this.exerciseProgressRepository = exerciseProgressRepository;
    }

    public DashboardStatsDto getDashboardStats(Long userId) {
        // Define the start of the current month
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);

        // 1. Fetch Streak from UserRepository
        // Assuming your User entity has a 'streak' field or findStreakById method
        int streak = userRepository.findStreakById(userId);

        // 2. Calculate volume using ExerciseProgressRepository
        // We divide by 1000.0 to convert kg into Tons (e.g., 4200kg -> 4.2t)
        Double rawVolume = exerciseProgressRepository.calculateMonthlyVolume(userId, startOfMonth);
        double volumeInTons = (rawVolume != null) ? rawVolume / 1000.0 : 0.0;

        // 3. Calculate success rate logic
        long scheduled = exerciseProgressRepository.countScheduledThisMonth(userId, startOfMonth);
        long completed = exerciseProgressRepository.countCompletedThisMonth(userId, startOfMonth);

        int rate = 0;
        if (scheduled > 0) {
            rate = (int) Math.round((double) completed / scheduled * 100);
        }

        // 4. Return the DTO
        return new DashboardStatsDto(streak, volumeInTons, rate);
    }
}
