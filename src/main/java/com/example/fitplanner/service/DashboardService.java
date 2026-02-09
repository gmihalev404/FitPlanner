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
        // 1. Calculate the sliding window (Today minus 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        // 2. Fetch Streak
        int streak = userRepository.findStreakById(userId);

        // 3. Calculate volume for the last 30 days
        Double rawVolume = exerciseProgressRepository.calculateMonthlyVolume(userId, thirtyDaysAgo);
        double volumeInTons = (rawVolume != null) ? rawVolume / 1000.0 : 0.0;

        // 4. Calculate success rate logic using the 30-day window
        long scheduled = exerciseProgressRepository.countScheduledLast30Days(userId, thirtyDaysAgo);

        // FIX: Use the 'Completed' specific repository method here
        long completed = exerciseProgressRepository.countCompletedLast30Days(userId, thirtyDaysAgo);

        System.out.println("Scheduled (30d): " + scheduled);
        System.out.println("Completed (30d): " + completed);

        int rate = 0;
        if (scheduled > 0) {
            rate = (int) Math.round((double) completed / scheduled * 100);
        }

        return new DashboardStatsDto(streak, volumeInTons, rate);
    }
}
