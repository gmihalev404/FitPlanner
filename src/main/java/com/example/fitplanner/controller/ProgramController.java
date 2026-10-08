package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ProgramDetailsDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.ProgramService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/programs")
public class ProgramController {
    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/fork/{id}") // Added leading slash
    public String forkProgram(@PathVariable Long id, HttpSession session) {
        UserDto loggedUser = (UserDto) session.getAttribute("loggedUser");

        if (loggedUser == null) {
            return "redirect:/login";
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
        ProgramDetailsDto programDetails = programService.getProgramDetails(id, userDto.getId());

        model.addAttribute("program", programDetails);

        // Вече не ти трябва отделен модел за weekDays,
        // защото те са вътре в programDetails.getWorkouts()
        return "program-details";
    }

    @PostMapping("/rate/{id}")
    public String rateProgram(@PathVariable Long id,
                              @RequestParam("rating") int stars,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in to rate programs.");
            return "redirect:/login";
        }

        try {
            programService.rateProgram(id, userDto.getId(), stars);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for rating this program.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/programs/details/" + id;
    }
}
