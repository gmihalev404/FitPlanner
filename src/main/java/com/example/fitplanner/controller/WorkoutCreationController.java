package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.SessionModelService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.*;

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
        CreatedProgramDto programForm = (CreatedProgramDto) session.getAttribute("programForm");
        if (programForm == null) {
            programForm = new CreatedProgramDto();
            programForm.setName("");
            programForm.setScheduleMonths(6);
            programForm.setRepeats(true);
            programForm.setNotifications(true);
            programForm.setIsPublic(false);
            session.setAttribute("programForm", programForm);
        }
        model.addAttribute("programForm", programForm);
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays == null) {
            weekDays = new ArrayList<>();
            DayOfWeek[] days = DayOfWeek.values();
            for (DayOfWeek day : days) {
                weekDays.add(new DayWorkout(day.name(), new ArrayList<>()));
            }
            session.setAttribute("weekDays", weekDays);
        } else {
            List<String> order = Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
            weekDays.sort(Comparator.comparingInt(day ->
                    order.indexOf(day.getDay().toUpperCase())));
        }

        model.addAttribute("weekDays", weekDays);
        return "create";
    }

    @GetMapping("/exercise-log")
    public String showExerciseLog(@RequestParam(required = false) String day,
                                  HttpSession session,
                                  Model model){
        if (session.getAttribute("loggedUser") == null) return "redirect:/login";
        sessionModelService.populateModel(session, model);
        if(day != null) session.setAttribute("currentDay", day);
        Set<ExerciseDto> exercises = exerciseService.getAll();
        model.addAttribute("exercises", exercises);
        return "exercises-log";
    }

    @GetMapping("/edit-exercise")
    public String showEditPage(@RequestParam String day,
                               @RequestParam Long exerciseId,
                               HttpSession session,
                               Model model) {
        sessionModelService.populateModel(session, model);
        session.setAttribute("currentDay", day);
        session.setAttribute("currentExerciseId", exerciseId);
        ExerciseDto exerciseDto = exerciseService.getById(exerciseId);
        session.setAttribute("exercise", exerciseDto);
        model.addAttribute("exercise", exerciseDto);
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equals(day)) {
                    for (ExerciseProgressDto ep : dw.getExercises()) {
                        if (ep.getExerciseId().equals(exerciseId)) {
                            model.addAttribute("dto", ep);
                            break;
                        }
                    }
                }
            }
        }
        return "set-exercise";
    }

    @PostMapping("/edit-exercise")
    public String editExercise(@ModelAttribute("dto") ExerciseProgressDto dto,
                               HttpSession session) {
        String day = (String) session.getAttribute("currentDay");
        Long exerciseId = (Long) session.getAttribute("currentExerciseId");
        if (day == null || exerciseId == null) return "redirect:/create";
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equals(day)) {
                    List<ExerciseProgressDto> exercises = dw.getExercises();
                    for (int i = 0; i < exercises.size(); i++) {
                        ExerciseProgressDto existing = exercises.get(i);
                        if (existing.getExerciseId().equals(exerciseId)) {
                            existing.setSets(dto.getSets());
                            existing.setReps(dto.getReps());
                            existing.setWeight(dto.getWeight());
                            existing.setName(existing.getName());
                            break;
                        }
                    }
                }
            }
        }
        session.setAttribute("weekDays", weekDays);
        return "redirect:/create";
    }

    @GetMapping("/set-exercise")
    public String showSelectedExercise(@RequestParam Long exerciseId,
                                       HttpSession session,
                                       Model model) {
        ExerciseDto exerciseDto = exerciseService.getById(exerciseId);
        sessionModelService.populateModel(session, model);
        session.removeAttribute("exercise");
        session.setAttribute("exercise", exerciseDto);
        model.addAttribute("exercise", exerciseDto);
        model.addAttribute("dto", new ExerciseProgressDto());
        return "set-exercise";
    }

    @PostMapping("/add-exercise")
    public String addExercise(@ModelAttribute("dto") ExerciseProgressDto dto,
                              HttpSession session,
                              Model model) {
        sessionModelService.populateModel(session, model);
        ExerciseDto exerciseDto = (ExerciseDto) session.getAttribute("exercise");
        String currentDay = (String) session.getAttribute("currentDay");
        if (exerciseDto == null || currentDay == null) return "redirect:/create";
        dto.setExerciseId(exerciseDto.getId());
        dto.setName(exerciseDto.getName());
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays == null) {
            weekDays = new ArrayList<>();
            session.setAttribute("weekDays", weekDays);
        }
        boolean added = false;
        for (DayWorkout dayWorkout : weekDays) {
            if (dayWorkout.getDay().equals(currentDay)) {
                if (dayWorkout.getExercises() == null) {
                    dayWorkout.setExercises(new ArrayList<>());
                }
                dayWorkout.getExercises().add(dto);
                added = true;
                break;
            }
        }
        if (!added) {
            DayWorkout newDay = new DayWorkout(currentDay, new ArrayList<>());
            newDay.getExercises().add(dto);
            weekDays.add(newDay);
        }
        session.setAttribute("weekDays", weekDays);
        List<ExerciseProgressDto> exerciseList = (List<ExerciseProgressDto>) session.getAttribute("exercise-list");
        if (exerciseList == null) {
            exerciseList = new ArrayList<>();
            session.setAttribute("exercise-list", exerciseList);
        }
        exerciseList.add(dto);
        return "redirect:/create";
    }

    @PostMapping("/remove-exercise")
    public String removeExercise(@RequestParam String day,
                                 @RequestParam Long exerciseId,
                                 HttpSession session) {
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equals(day)) {
                    dw.getExercises().removeIf(ep -> ep.getExerciseId().equals(exerciseId));
                    break;
                }
            }
        }
        session.setAttribute("weekDays", weekDays);
        return "redirect:/create";
    }

    @PostMapping("/show/{id}")
    public String showExercise(@PathVariable Long id, HttpSession session, Model model){
        sessionModelService.populateModel(session, model);
        ExerciseDto exerciseDto = exerciseService.getById(id);
        if (exerciseDto == null) return "redirect:/exercise-log";
        session.setAttribute("currentExercise", exerciseDto);
        model.addAttribute("src", exerciseDto.getVideoUrl());
        return "show";
    }

    @PostMapping("/close-show")
    public String closeShow(HttpSession session){
        session.removeAttribute("currentExercise");
        return "redirect:/exercise-log";
    }

    @PostMapping("/close-set")
    public String closeSetExercise(HttpSession session){
        session.removeAttribute("exercise");
        session.removeAttribute("currentDay");
        return "redirect:/exercise-log";
    }

    @PostMapping("/create-full-workout")
    public String createFullWorkout(@ModelAttribute("programForm") CreatedProgramDto dto,
                                    HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        dto.setWeekDays(weekDays);
        programService.createProgram(dto, userDto);
        sessionModelService.clearSession(session);
        return "redirect:/home";
    }

    @PostMapping("/close-log")
    public String closeLog() {
        return "redirect:/create";
    }

    @PostMapping("/update-program-session")
    @ResponseBody
    public void updateProgramSession(@ModelAttribute CreatedProgramDto programFormInput,
                                     HttpSession session) {
        CreatedProgramDto sessionProgramForm = (CreatedProgramDto) session.getAttribute("programForm");
        if (sessionProgramForm == null) {
            sessionProgramForm = new CreatedProgramDto();
        }
        sessionProgramForm.setName(programFormInput.getName());
        sessionProgramForm.setScheduleMonths(programFormInput.getScheduleMonths());
        sessionProgramForm.setNotifications(programFormInput.getNotifications());
        sessionProgramForm.setIsPublic(programFormInput.getIsPublic());
        session.setAttribute("programForm", sessionProgramForm);
        CreatedProgramDto sessionProgramForm1 = (CreatedProgramDto) session.getAttribute("programForm");
        System.out.println();
    }
}