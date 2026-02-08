package com.example.fitplanner.controller;

import com.example.fitplanner.dto.NotificationDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;

@Controller
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/read/{id}")
    public String markAsReadAndRedirect(@PathVariable Long id) {
        // 1. Mark as checked and get the destination URL
        String redirectUrl = notificationService.markAsRead(id);

        // 2. Redirect the user to the program details or session page
        return "redirect:" + (redirectUrl != null ? redirectUrl : "/search");
    }
}