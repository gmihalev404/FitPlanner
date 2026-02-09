package com.example.fitplanner.service;

import com.example.fitplanner.dto.NotificationDto;
import com.example.fitplanner.entity.model.Notification;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.entity.model.WorkoutSession;
import com.example.fitplanner.repository.NotificationRepository;
import com.example.fitplanner.repository.WorkoutSessionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final ModelMapper modelMapper;

    // Runs every day at 04:00 PM
    @Scheduled(initialDelay = 5000, fixedDelay = 3600000) // 5 сек след старт
    public void checkAndSendDailyWorkoutNotifications() {
        LocalDate today = LocalDate.now();
        System.out.println("DEBUG: Cron task started for date: " + today);

        List<WorkoutSession> sessions = workoutSessionRepository
                .findAllByScheduledForAndProgramNotificationsTrue(today);

        var uniqueNotifications = sessions.stream()
                .collect(Collectors.toMap(
                        session -> session.getUser().getId() + "-" + session.getProgram().getId(), // Ключ
                        session -> session,
                        (existing, replacement) -> existing // Ако има дубликат, запази първия намерен
                ));

        System.out.println("DEBUG: Found sessions: " + sessions.size());
        System.out.println("DEBUG: Unique notifications to send: " + uniqueNotifications.size());

        uniqueNotifications.values().forEach(session -> {
            System.out.println("DEBUG: Sending notification to: " + session.getUser().getEmail() +
                    " for program: " + session.getProgram().getName());

            sendSystemNotification(
                    session.getUser(),
                    "Workout Reminder",
                    "Time to hit the gym! You have a workout today for: " + session.getProgram().getName(),
                    "/my-workouts?programId=" + session.getProgram().getId()
            );
        });
    }

    private void sendSystemNotification(User observer, String name, String desc, String url) {
        Notification notification = new Notification();
        notification.setObserver(observer);
        notification.setSender(null);
        notification.setName(name);
        notification.setDescription(desc);
        notification.setUrl(url);
        notification.setDate(LocalDateTime.now());
        notification.setChecked(false);

        notificationRepository.saveAndFlush(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByUser(userId)
                .stream()
                .map(n -> modelMapper.map(n, NotificationDto.class))
                .collect(Collectors.toList());
    }

    public String markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        notification.setChecked(true);
        notificationRepository.saveAndFlush(notification);

        return notification.getUrl();
    }
}
