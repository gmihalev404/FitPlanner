package com.example.fitplanner.controller;

import com.example.fitplanner.dto.NotificationDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;



@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/read/{id}")
    public String markAsReadAndRedirect(@PathVariable Long id, HttpSession session) {
        // 1. Проверка дали потребителят е логнат
        UserDto currentUser = (UserDto) session.getAttribute("loggedUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            // 2. Тук може да добавите проверка в сервиза дали нотификацията принадлежи на currentUser.getId()
            String redirectUrl = notificationService.markAsRead(id);

            return "redirect:" + (redirectUrl != null && !redirectUrl.isEmpty() ? redirectUrl : "/dashboard");
        } catch (EntityNotFoundException e) {
            // Ако нотификацията не съществува, не чупим приложението
            return "redirect:/dashboard";
        }
    }
}