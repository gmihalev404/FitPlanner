package com.example.fitplanner.controller;

import com.example.fitplanner.dto.DayWorkout;
import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.SessionModelService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class WorkoutCreationController {

    private final UserService userService;
    private final ProgramService programService;
    private final ExerciseService exerciseService;
    private final SessionModelService sessionModelService;

    public WorkoutCreationController(UserService userService,
                                     ProgramService programService,
                                     ExerciseService exerciseService,
                                     SessionModelService sessionModelService) {
        this.userService = userService;
        this.programService = programService;
        this.exerciseService = exerciseService;
        this.sessionModelService = sessionModelService;
    }

    @GetMapping("/create")
    public String createWorkout(HttpSession session, Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("loggedUser");
        if (sessionUser == null) return "redirect:/login";
        sessionModelService.populateModel(session, model);
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays == null) {
            weekDays = new ArrayList<>();
            LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
            for (int i = 0; i < 7; i++) {
                String dayKey = startOfWeek.plusDays(i).getDayOfWeek().name().toLowerCase();
                weekDays.add(new DayWorkout(dayKey, new ArrayList<>()));
            }
            session.setAttribute("weekDays", weekDays);
        }
        model.addAttribute("weekDays", weekDays);
        return "create";
    }

    @PostMapping("/add-exercise/{day}")
    public String addExerciseToDay(@PathVariable String day, HttpSession session) {
        session.setAttribute("currentDay", day);
        return "redirect:/exercise-log";
    }
    @PostMapping("/edit-exercise/{day}/{exerciseId}")
    public String editExercise(@PathVariable String day,
                               @PathVariable Long exerciseId,
                               HttpSession session) {
        session.setAttribute("currentDay", day);
        session.setAttribute("currentExerciseId", exerciseId);
        sessionModelService.populateModel(session, null);
        return "redirect:/exercise-log/edit";
    }

    @GetMapping("/exercise-log")
    public String showExerciseLog(HttpSession session, Model model){
        sessionModelService.populateModel(session, model);
        if (session.getAttribute("loggedUser") == null) return "redirect:/login";
        Set<ExerciseDto> exercises = exerciseService.getAll();
        model.addAttribute("exercises", exercises);
        return "exercises-log";
    }

    @PostMapping("/show/{id}")
    public String showExercise(@PathVariable Long id, HttpSession session, Model model){
        sessionModelService.populateModel(session, model);
        ExerciseDto exerciseDto = exerciseService.getById(id);
        model.addAttribute("src", exerciseDto.getVideoUrl());
        return "show";
    }

    @PostMapping("/close-show")
    public String closeShow(){
        return "redirect:/exercise-log";
    }
}