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

        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        Integer streakValue = userRepository.findStreakById(userId);
        int streak = streakValue != null ? streakValue : 0;

        Object[] stats =
                exerciseProgressRepository.getDashboardAggregates(
                        userId,
                        thirtyDaysAgo
                );

        double rawVolume =
                stats[0] != null
                        ? ((Number) stats[0]).doubleValue()
                        : 0.0;

        long scheduled =
                stats[1] != null
                        ? ((Number) stats[1]).longValue()
                        : 0L;

        long completed =
                stats[2] != null
                        ? ((Number) stats[2]).longValue()
                        : 0L;

        double volumeInTons = rawVolume / 1000.0;

        int rate = 0;

        if (scheduled > 0) {
            rate = (int) Math.round(
                    (double) completed / scheduled * 100
            );
        }

        return new DashboardStatsDto(
                streak,
                volumeInTons,
                rate
        );
    }
}
