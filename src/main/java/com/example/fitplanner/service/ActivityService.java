package com.example.fitplanner.service;

import com.example.fitplanner.dto.RecentActivityDto;
import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.entity.model.Milestone;
import com.example.fitplanner.repository.ExerciseProgressRepository;
import com.example.fitplanner.repository.MilestoneRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ActivityService {

    private final ExerciseProgressRepository exerciseProgressRepository;
    private final MilestoneRepository milestoneRepository;

    public ActivityService(ExerciseProgressRepository exerciseProgressRepository,
                           MilestoneRepository milestoneRepository) {
        this.exerciseProgressRepository = exerciseProgressRepository;
        this.milestoneRepository = milestoneRepository;
    }

    /**
     * Fetches a combined list of recent exercises and milestones,
     * sorted chronologically.
     */
    public List<RecentActivityDto> getRecentActivity(Long userId) {
        // 1. Fetch recent exercises (Fetch 5 to ensure variety after sorting)
        List<ExerciseProgress> recentExercises = exerciseProgressRepository
                .findRecentCompletedExercises(userId, PageRequest.of(0, 5));

        // 2. Fetch recent milestones (Fetch 5 to ensure variety after sorting)
        List<Milestone> recentMilestones = milestoneRepository
                .findRecentMilestones(userId, PageRequest.of(0, 4));

        // 3. Combine both streams, sort by the actual date, and limit to top 5
        return Stream.concat(
                        recentExercises.stream().map(this::mapToExerciseDto),
                        recentMilestones.stream().map(this::mapToMilestoneDto)
                )
                .sorted(Comparator.comparing(RecentActivityDto::sortDate).reversed())
                .limit(5) // Increased from 3 to 5 to make the dashboard look better
                .toList();
    }

    private RecentActivityDto mapToExerciseDto(ExerciseProgress ep) {
        // This creates "Bench 100kg"
        String formattedTitle = ep.getExercise().getName() + " " + Math.round(ep.getWeight()) + "kg";

        return new RecentActivityDto(
                formattedTitle,
                formatTimeAgo(ep.getLastCompleted()),
                "#28a745", // Green dot
                ep.getLastCompleted().atStartOfDay()
        );
    }

    private RecentActivityDto mapToMilestoneDto(Milestone m) {
        // This creates "Milestone: [Title]"
        return new RecentActivityDto(
                "Milestone: " + m.getTitle(),
                formatTimeAgo(m.getAchievedAt().toLocalDate()),
                "#007bff", // Blue dot
                m.getAchievedAt()
        );
    }

    private String formatTimeAgo(LocalDate date) {
        if (date == null) return "Recently";

        long days = ChronoUnit.DAYS.between(date, LocalDate.now());

        if (days == 0) return "Today";
        if (days == 1) return "Yesterday";
        if (days < 7) return days + " days ago";
        if (days < 30) return (days / 7) + " weeks ago";

        return "Over a month ago";
    }
}