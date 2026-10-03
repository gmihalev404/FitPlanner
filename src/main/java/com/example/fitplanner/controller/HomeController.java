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

            long totalStart = System.nanoTime();

            long start = System.nanoTime();
            List<ForkableProgramDto> recommended =
                    programService.getRecommendedPrograms(userDto.getId());
            System.out.println("PERF recommendedPrograms: "
                    + (System.nanoTime() - start) / 1_000_000 + " ms");

            start = System.nanoTime();
            DashboardStatsDto stats =
                    dashboardService.getDashboardStats(userDto.getId());
            System.out.println("PERF dashboardStats: "
                    + (System.nanoTime() - start) / 1_000_000 + " ms");

            start = System.nanoTime();
            List<RecentActivityDto> recentActivities =
                    activityService.getRecentActivity(userDto.getId());
            System.out.println("PERF recentActivity: "
                    + (System.nanoTime() - start) / 1_000_000 + " ms");

            start = System.nanoTime();
            QuoteDto quote = quoteService.getRandomQuote();
            System.out.println("PERF randomQuote: "
                    + (System.nanoTime() - start) / 1_000_000 + " ms");


            model.addAttribute("recommendedPrograms", recommended);
            model.addAttribute("userStats", stats);
            model.addAttribute("recentActivities", recentActivities);
            model.addAttribute("dailyQuote", quote);

            System.out.println("PERF HOME TOTAL SERVICES: "
                    + (System.nanoTime() - totalStart) / 1_000_000 + " ms");
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