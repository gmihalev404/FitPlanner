package com.example.fitplanner.controller;

import com.example.fitplanner.dto.DayWorkout;
import com.example.fitplanner.dto.ProgramDetailsDto;
import com.example.fitplanner.dto.ProgramDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.UserService;
import com.example.fitplanner.service.WorkoutSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/programs")
public class ProgramController {
    private final ProgramService programService;
    private final UserService userService;

    public ProgramController(ProgramService programService, UserService userService) {
        this.programService = programService;
        this.userService = userService;
    }

    @PostMapping("/fork/{id}") // Added leading slash
    public String forkProgram(@PathVariable Long id, HttpSession session) {
        UserDto loggedUser = (UserDto) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            // Ensure this matches your actual login GET route (likely /users/login)
            return "redirect:/users/login";
        }

        programService.forkProgram(id, loggedUser.getId());

        // Redirecting to /search is fine if that's where your global discovery is
        return "redirect:/search";
    }

    @GetMapping("details/{id}")
    public String getProgramDetails(@PathVariable Long id, Model model, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        // Използваме новия метод, който събира всичко в едно DTO
        // Този метод трябва да връща ProgramDetailsDto
        ProgramDetailsDto programDetails = programService.getProgramDetails(id);

        model.addAttribute("program", programDetails);

        // Вече не ти трябва отделен модел за weekDays,
        // защото те са вътре в programDetails.getWorkouts()
        return "program-details";
    }

    @PostMapping("/rate/{id}")
    public String rateProgram(@PathVariable Long id, @RequestParam("rating") int stars, RedirectAttributes redirectAttributes) {
        programService.addRating(id, stars);
        redirectAttributes.addFlashAttribute("successMessage", "Благодарим ви за оценката!");
        return "redirect:/programs/details/" + id;
    }
}
