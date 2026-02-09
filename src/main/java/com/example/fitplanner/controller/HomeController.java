package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {
    private final ExerciseService exerciseService;
    private final UserService userService;
    private final ProgramService programService;
    private final DashboardService dashboardService;
    private final QuoteService quoteService;
    private final ActivityService activityService;

    public HomeController(ExerciseService exerciseService, UserService userService, ProgramService programService, DashboardService dashboardService, QuoteService quoteService, ActivityService activityService) {
        this.exerciseService = exerciseService;
        this.userService = userService;
        this.programService = programService;
        this.dashboardService = dashboardService;
        this.quoteService = quoteService;
        this.activityService = activityService;
    }

    @GetMapping("/home")
    public String getHome(HttpSession session, Model model) {
        session.removeAttribute("programForm");
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        model.addAttribute("userDto", userDto);
        session.removeAttribute("sessionExercises");
//        if (model.getAttribute("userDto") == null) return "redirect:/login";

        if (userDto != null) {
            // 2. Fetch some sample programs to show
            // You can use a custom repository method like findTop2ByOrderByRatingDesc()
            List<ForkableProgramDto> recommended = programService.getRecommendedPrograms(userDto.getId());

            // 3. Add to model - MUST match the name in HTML: "recommendedPrograms"
            model.addAttribute("recommendedPrograms", recommended);
            model.addAttribute("userStats", dashboardService.getDashboardStats(userDto.getId()));
            model.addAttribute("recentActivities", activityService.getRecentActivity(userDto.getId()));
            model.addAttribute("dailyQuote", quoteService.getRandomQuote());
        }

        return "home";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/exercise-library")
    public String viewAllExercises(Model model, HttpSession session) {
        model.addAttribute("exercises", exerciseService.getAll());
        return "exercise-library";
    }

    // View specific details for one exercise
    @GetMapping("/details/{id}")
    public String showDetails(@PathVariable Long id, Model model, HttpSession session) {
        ExerciseDto exercise = exerciseService.getById(id);
        model.addAttribute("exercise", exercise);
        return "exercise-details";
    }
}