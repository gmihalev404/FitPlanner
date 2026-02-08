package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class CopyProgramController {
    private final ProgramService programService;
    private final UserService userService;

    public CopyProgramController(ProgramService programService, UserService userService) {
        this.programService = programService;
        this.userService = userService;
    }

    @PostMapping("/programs/fork/{id}")
    public String forkProgram(@PathVariable Long id, HttpSession session) {
        // 1. Retrieve the custom session object
        // Assuming you stored it as "loggedUser" during login
        UserDto loggedUser = (UserDto) session.getAttribute("loggedUser");

        // 2. Guard clause: if session expired or user isn't logged in
        if (loggedUser == null) {
            return "redirect:/users/login";
        }

        // 3. Use the ID from your session DTO to call the service
        // This avoids the ClassCastException because we pass a Long, not the DTO
        programService.forkProgram(id, loggedUser.getId());

        return "redirect:/search";
    }
}
