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
        LocalDate today = LocalDate.now();

        // LOGIC: A session is read-only if it was manually finished OR if the day has passed.
        boolean isFinished = workoutSessionService.isSessionFinished(userDto.getId(), date);
        boolean isPast = date.isBefore(today);
        boolean viewOnly = isFinished || isPast;

        List<ExerciseSessionDto> exercises = workoutSessionService
                .getWorkoutsByProgramIdsAndDate(programIds, date, userDto.getMeasuringUnits(), userDto.getId());

        // Calculate progress for the progress bar
        int totalSets = exercises.stream().mapToInt(ExerciseSessionDto::getSets).sum();
        int completedSets = exercises.stream().mapToInt(ex ->
                workoutSessionService.getSetsCompletedForExercise(userDto.getId(), ex.getExerciseId(), date)).sum();

        long progressPercent = (totalSets > 0) ? Math.round(((double) completedSets / totalSets) * 100) : 0;

        // Map existing database completion counts to the DTOs
        exercises.forEach(ex -> ex.setSetsCompleted(
                workoutSessionService.getSetsCompletedForExercise(userDto.getId(), ex.getExerciseId(), date)
        ));

        WorkoutResultWrapper wrapper = new WorkoutResultWrapper();

        model.addAttribute("exercises", exercises);
        model.addAttribute("workoutForm", wrapper);
        model.addAttribute("date", date);
        model.addAttribute("viewOnly", viewOnly); // Controls JS and Submit button visibility
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("userDto", userDto);

        // Store exercises in session for the POST method to verify changes
        session.setAttribute("sessionExercises", exercises);

        return "workout-session";
    }

    @PostMapping("/finishSession")
    public String finishWorkoutSession(
            @ModelAttribute("workoutForm") WorkoutResultWrapper form,
            @RequestParam String date,
            @RequestParam(required = false) List<Long> acceptedIncreaseIds,
            @RequestParam(required = false) List<Long> declinedIncreaseIds,
            HttpSession session) {

        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        LocalDate sessionDate = LocalDate.parse(date);
        LocalDate today = LocalDate.now();

        // SECURITY CHECK: Block saving if the date is in the past
        // This prevents users from editing their history even if they bypass the UI
        if (sessionDate.isBefore(today)) {
            return "redirect:/my-workouts?error=past_date_immutable";
        }

        List<ExerciseSessionDto> exercises = (List<ExerciseSessionDto>) session.getAttribute("sessionExercises");

        // 1. Process suggested weight increases/decreases
        if (exercises != null) {
            if (acceptedIncreaseIds != null) {
                for (Long id : acceptedIncreaseIds) {
                    workoutSessionService.handleSuggestedChangeDecision(exercises, id, true);
                }
            }
            if (declinedIncreaseIds != null) {
                for (Long id : declinedIncreaseIds) {
                    workoutSessionService.handleSuggestedChangeDecision(exercises, id, false);
                }
            }
        }
        System.out.println(form);

        // 2. Save progress to ExerciseProgress and update WorkoutSession as 'finished'
        workoutSessionService.finishSession(form.getResults(), sessionDate, userDto.getId());

        session.removeAttribute("sessionExercises");
        return "redirect:/my-workouts";
    }
}