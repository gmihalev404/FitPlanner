package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ExerciseSessionDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.dto.WorkoutResultWrapper;
import com.example.fitplanner.service.WorkoutSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class WorkoutSessionController {
    private final WorkoutSessionService workoutSessionService;

    public WorkoutSessionController(WorkoutSessionService workoutSessionService) {
        this.workoutSessionService = workoutSessionService;
    }

    @GetMapping("/workout-session")
    public String showWorkoutSession(@RequestParam Integer year,
                                     @RequestParam Integer month,
                                     @RequestParam Integer day,
                                     @RequestParam(required = false) List<Long> programIds,
                                     Model model,
                                     HttpSession session) {

        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        LocalDate date = LocalDate.of(year, month, day);
        boolean isFinished = workoutSessionService.isSessionFinished(userDto.getId(), date);

        List<ExerciseSessionDto> exercises = workoutSessionService
                .getWorkoutsByProgramIdsAndDate(programIds, date, userDto.getMeasuringUnits(), userDto.getId());

        // Calculate progress
        int totalSets = exercises.stream().mapToInt(ExerciseSessionDto::getSets).sum();
        int completedSets = exercises.stream().mapToInt(ex ->
                workoutSessionService.getSetsCompletedForExercise(userDto.getId(), ex.getExerciseId(), date)).sum();

        long progressPercent = (totalSets > 0) ? Math.round(((double) completedSets / totalSets) * 100) : 0;

        // Populate DTOs with current DB values
        exercises.forEach(ex -> ex.setSetsCompleted(workoutSessionService.getSetsCompletedForExercise(userDto.getId(), ex.getExerciseId(), date)));

        WorkoutResultWrapper wrapper = new WorkoutResultWrapper();
        model.addAttribute("exercises", exercises);
        model.addAttribute("workoutForm", wrapper);
        model.addAttribute("date", date); // Passed to hidden input in HTML
        model.addAttribute("viewOnly", isFinished);
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("userDto", userDto);

        // Required for suggestion logic in POST
        session.setAttribute("sessionExercises", exercises);

        return "workout-session";
    }

    @PostMapping("/finishSession")
    public String finishWorkoutSession(
            @ModelAttribute("workoutForm") WorkoutResultWrapper form,
            @RequestParam String date, // Spring is looking for a field named "date"
            @RequestParam(required = false) List<Long> acceptedIncreaseIds,
            @RequestParam(required = false) List<Long> declinedIncreaseIds,
            HttpSession session) {

        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        LocalDate sessionDate = LocalDate.parse(date);
        List<ExerciseSessionDto> exercises = (List<ExerciseSessionDto>) session.getAttribute("sessionExercises");

        // 1. Handle Suggested Increases
        if (exercises != null) {
            if (acceptedIncreaseIds != null) {
                for (Long id : acceptedIncreaseIds) workoutSessionService.handleSuggestedChangeDecision(exercises, id, true);
            }
            if (declinedIncreaseIds != null) {
                for (Long id : declinedIncreaseIds) workoutSessionService.handleSuggestedChangeDecision(exercises, id, false);
            }
        }

        // 2. Save Session Data & Mark Finished
        workoutSessionService.finishSession(form.getResults(), sessionDate, userDto.getId());

        session.removeAttribute("sessionExercises");
        return "redirect:/my-workouts";
    }
}