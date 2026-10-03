package com.example.fitplanner.controller;

import com.example.fitplanner.dto.NotificationDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @ModelAttribute
    public void addNotifications(HttpSession session, Model model) {

        UserDto user = (UserDto) session.getAttribute("loggedUser");

        if (user == null) {
            user = (UserDto) session.getAttribute("userDto");
        }

        if (user == null) {
            model.addAttribute("unreadNotificationsCount", 0);
            model.addAttribute("allUnreadNotifications", Collections.emptyList());
            return;
        }

        List<NotificationDto> unread =
                notificationService.getUnreadNotifications(user.getId());

        model.addAttribute("unreadNotificationsCount", unread.size());
        model.addAttribute("allUnreadNotifications", unread);
    }
}