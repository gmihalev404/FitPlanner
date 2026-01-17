package com.example.fitplanner.controller;

import com.example.fitplanner.dto.NotificationDto;
import com.example.fitplanner.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;

@Controller
public class NotificationController {
    @GetMapping("notifications")
    public String showNotification(HttpSession session, Model model){
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        model.addAttribute("notification", userDto.getNotifications()
                .stream()
                .sorted(Comparator.comparing(NotificationDto::getDate).reversed()));
//                .limit(10));
        return "/notifications";
    }
}