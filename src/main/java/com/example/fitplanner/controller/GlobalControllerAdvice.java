package com.example.fitplanner.controller;

import com.example.fitplanner.dto.NotificationDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final NotificationService notificationService;

    @ModelAttribute("unreadNotificationsCount")
    public int getUnreadCount(HttpSession session) {
        // Опитай да вземеш потребителя и по двата възможни ключа
        UserDto user = (UserDto) session.getAttribute("loggedUser");
        if (user == null) {
            user = (UserDto) session.getAttribute("userDto");
        }

        if (user != null) {
            return notificationService.getUnreadNotifications(user.getId()).size();
        }
        return 0;
    }

    @ModelAttribute("allUnreadNotifications")
    public List<NotificationDto> getAllUnread(HttpSession session) {
        UserDto user = (UserDto) session.getAttribute("loggedUser");
        if (user == null) {
            user = (UserDto) session.getAttribute("userDto");
        }

        if (user != null) {
            return notificationService.getUnreadNotifications(user.getId());
        }
        return Collections.emptyList();
    }
}
