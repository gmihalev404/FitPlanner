package com.example.fitplanner.controller;

import com.example.fitplanner.dto.DayWorkout;
import com.example.fitplanner.dto.ExerciseProgressDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.entity.model.WorkoutSession;
import com.example.fitplanner.entity.model.ExerciseProgress;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.ExerciseProgressService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WorkoutCreationController {

    private final UserService userService;
    private final ProgramService programService;
    private final ExerciseProgressService exerciseProgressService;

    @Autowired
    public WorkoutCreationController(UserService userService, ProgramService programService,
                                     ExerciseProgressService exerciseProgressService) {
        this.userService = userService;
        this.programService = programService;
        this.exerciseProgressService = exerciseProgressService;
    }

    @GetMapping("/create/{id}")
    public String createWorkout(@PathVariable Long id,
                                HttpSession session,
                                Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("loggedInUser");
        if (sessionUser == null || !sessionUser.getId().equals(id)) {
            return "redirect:/login";
        }
        session.setAttribute("loggedInUser", sessionUser);
        session.setAttribute("theme",
                sessionUser.getTheme() != null ? sessionUser.getTheme() : "dark");
        session.setAttribute("language",
                sessionUser.getLanguage() != null ? sessionUser.getLanguage() : "en");
        session.setAttribute("units",
                sessionUser.getMeasuringUnits() != null ? sessionUser.getMeasuringUnits() : "kg");
        model.addAttribute("userDto", sessionUser);
        List<DayWorkout> weekDays =
                (List<DayWorkout>) session.getAttribute("weekDaysDraft");
        if (weekDays == null) {
            weekDays = new ArrayList<>();
            LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
            for (int i = 0; i < 7; i++) {
                LocalDate date = startOfWeek.plusDays(i);
                String dayKey = "day." + date.getDayOfWeek().name().toLowerCase();
                weekDays.add(new DayWorkout(dayKey, new ArrayList<>()));
            }
            session.setAttribute("weekDaysDraft", weekDays);
        }
        model.addAttribute("weekDays", weekDays);
        return "create";
    }

    @PostMapping("/add-exercise/{day}/{id}")
    public String addExerciseToDay(@PathVariable String day,
                                   @PathVariable Long id,
                                   HttpSession session) {
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays == null) return "redirect:/create/" + id;
        session.setAttribute("currentDay", day);
        return "redirect:/exercise-log";
    }

    @PostMapping("/edit-exercise/{day}/{exerciseId}/{id}")
    public String editExercise(@PathVariable String day,
                               @PathVariable Long exerciseId,
                               @PathVariable Long id,
                               HttpSession session) {
        session.setAttribute("currentDay", day);
        session.setAttribute("currentExerciseId", exerciseId);
        return "redirect:/exercise-log/edit";
    }
}