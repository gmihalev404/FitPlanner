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

    // Runs every day at 07:00 AM
    @Scheduled(cron = "0 30 20 * * *")
    @Transactional
    public void checkAndSendDailyWorkoutNotifications() {
        LocalDate today = LocalDate.now();

        // Fetch sessions for today where program.notifications == true
        List<WorkoutSession> sessions = workoutSessionRepository
                .findAllByScheduledForAndProgramNotificationsTrue(today);

        for (WorkoutSession session : sessions) {
            sendSystemNotification(
                    session.getUser(),
                    "Workout Reminder",
                    "Time to hit the gym! You have a session today for: " + session.getProgram().getName(),
                    "/programs/details/" + session.getProgram().getId()
            );
        }
    }

    private void sendSystemNotification(User observer, String name, String desc, String url) {
        Notification notification = new Notification();
        notification.setObserver(observer);
        notification.setSender(null); // Null represents "System"
        notification.setName(name);
        notification.setDescription(desc);
        notification.setUrl(url);
        notification.setDate(LocalDateTime.now());
        notification.setChecked(false);

        notificationRepository.save(notification);
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
        notificationRepository.save(notification);

        return notification.getUrl();
    }
}
