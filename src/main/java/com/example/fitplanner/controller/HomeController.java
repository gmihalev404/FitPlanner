package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.dto.ForkableProgramDto;
import com.example.fitplanner.dto.ProgramDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.entity.model.Exercise;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.UserService;
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

    public HomeController(ExerciseService exerciseService, UserService userService, ProgramService programService) {
        this.exerciseService = exerciseService;
        this.userService = userService;
        this.programService = programService;
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
        }

        return "home";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }

    @GetMapping("/exercise-library")
    public String viewAllExercises(Model model) {
        model.addAttribute("exercises", exerciseService.getAll());
        return "exercise-library";
    }

    // View specific details for one exercise
    @GetMapping("/details/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        ExerciseDto exercise = exerciseService.getById(id);
        model.addAttribute("exercise", exercise);
        return "exercise-details";
    }
}