package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/stats")
public class StatisticsController {

    private final UserService userService;
    private final ExerciseService exerciseService;

    public StatisticsController(UserService userService, ExerciseService exerciseService) {
        this.userService = userService;
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public String showStatsPage(HttpSession session, Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        // Fetch the unified Stats DTO once
        StatsUserDto statsUser = userService.getById(userDto.getId(), StatsUserDto.class);

        // Store in session to avoid DB calls in the AJAX endpoint later
        session.setAttribute("userStatsCache", statsUser);

        // -----------------------------
        // Prepare Weight Change Data
        // -----------------------------
        List<String> personalLabels = new ArrayList<>();
        List<Double> personalData = new ArrayList<>();

        if (statsUser.getWeightChanges() != null) {
            statsUser.getWeightChanges().stream()
                    .sorted(Comparator.comparing(WeightEntryDto::getDate)) // oldest → newest
                    .forEach(entry -> {
                        personalLabels.add(entry.getDate().toString());
                        personalData.add(entry.getWeight());
                    });
        }

        // -----------------------------
        // Get all exercises for the grid/selection
        // -----------------------------
        List<ExerciseDto> exercises = exerciseService.getAll();

        // -----------------------------
        // Add attributes to the model
        // -----------------------------
        model.addAttribute("exercises", exercises);
        model.addAttribute("personalLabels", personalLabels);
        model.addAttribute("personalData", personalData);
        model.addAttribute("units", statsUser.getMeasuringUnits());
        model.addAttribute("theme", userDto.getTheme());
        model.addAttribute("language", userDto.getLanguage());

        return "stats";
    }

    @GetMapping("/exercise/{id}")
    @ResponseBody
    public Map<String, Object> getExerciseStats(@PathVariable Long id, HttpSession session) {
        StatsUserDto statsUser = (StatsUserDto) session.getAttribute("userStatsCache");
        Map<String, Object> response = new HashMap<>();

        if (statsUser == null) return response;

        List<StatsExerciseDto> filteredStats = statsUser.getProgresses().stream()
                .filter(s -> s.getExerciseId().equals(id))
                .filter(s -> s.getCompletedDate() != null)
                .sorted(Comparator.comparing(StatsExerciseDto::getCompletedDate))
                .toList();

        response.put("labels", filteredStats.stream()
                .map(s -> s.getCompletedDate().toString()).toList());

        response.put("values", filteredStats.stream()
                .map(StatsExerciseDto::getWeight).toList());

        response.put("unit", statsUser.getMeasuringUnits());

        return response;
    }
}