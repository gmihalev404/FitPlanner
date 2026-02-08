package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.ExerciseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public String viewAllExercises(HttpSession session, Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (!userDto.getEnabled()) {
            session.invalidate();
            return "redirect:/login?banned=true";
        }
        List<ExerciseDto> exercises = exerciseService.getAll();
        model.addAttribute("exercises", exercises);
        return "exercise-library";
    }

    @GetMapping("/details/{id}")
    public String showDetails(@PathVariable Long id, Model model, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (!userDto.getEnabled()) {
            session.invalidate();
            return "redirect:/login?banned=true";
        }
        ExerciseDto exercise = exerciseService.getById(id);
        model.addAttribute("exercise", exercise);
        return "exercise-details";
    }
}