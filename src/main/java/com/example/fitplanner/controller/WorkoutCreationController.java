package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.FileService;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.util.*;

@Controller
public class WorkoutCreationController {
    private final UserService userService;
    private final ProgramService programService;
    private final ExerciseService exerciseService;
    private final FileService fileService;

    public WorkoutCreationController(UserService userService,
                                     ProgramService programService,
                                     ExerciseService exerciseService,
                                     FileService fileService) {
        this.userService = userService;
        this.programService = programService;
        this.exerciseService = exerciseService;
        this.fileService = fileService;
    }

    @GetMapping("/create/new")
    public String startNewWorkout(HttpSession session) {
        session.removeAttribute("weekDays");
        session.removeAttribute("programForm");
        session.removeAttribute("currentDay");
        session.removeAttribute("exercise");
        session.removeAttribute("programId");
        return "redirect:/create";
    }

    @GetMapping("/create")
    public String createWorkout(@RequestParam(required = false) Boolean newProgram,
                                HttpSession session,
                                Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        // --- THE CRITICAL RESET ---
        // If the URL is /create?newProgram=true, we clear everything.
        // This stops "Edit Mode" data from leaking into "New Mode".
        if (Boolean.TRUE.equals(newProgram)) {
            session.removeAttribute("weekDays");
            session.removeAttribute("programForm");
            session.removeAttribute("programId");
            session.removeAttribute("exercise");
            session.removeAttribute("currentDay");
        }

        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);

        // Standard initialization if session is empty
        CreatedProgramDto programForm = (CreatedProgramDto) session.getAttribute("programForm");
        if (programForm == null) {
            programForm = new CreatedProgramDto();
            programForm.setName("");
            programForm.setScheduleMonths(6);
            // Public by default for Trainers/Admins
            programForm.setIsPublic(userDto.getRole().equals(Role.TRAINER) || userDto.getRole().equals(Role.ADMIN));
            session.setAttribute("programForm", programForm);
        }

        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays == null) {
            weekDays = new ArrayList<>();
            for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
                weekDays.add(new DayWorkout(day.name(), new ArrayList<>()));
            }
            session.setAttribute("weekDays", weekDays);
        }

        model.addAttribute("programForm", programForm);
        model.addAttribute("weekDays", weekDays);
        model.addAttribute("programId", session.getAttribute("programId"));
        model.addAttribute("userDto", userDto);
        model.addAttribute("programsUserDto", programsUserDto);

        return "create";
    }
    @GetMapping("/exercise-log")
    public String showExerciseLog(@RequestParam(required = false) String day,
                                  HttpSession session,
                                  Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("loggedUser");
        if (sessionUser == null) return "redirect:/login";

        UserDto userDto = userService.getById(sessionUser.getId(), UserDto.class);
        ProgramsUserDto programsUserDto = userService.getById(sessionUser.getId(), ProgramsUserDto.class);

        if (day != null) session.setAttribute("currentDay", day);

        List<ExerciseDto> exercises = exerciseService.getAll();
        model.addAttribute("exercises", exercises);
        model.addAttribute("userDto", userDto);
        model.addAttribute("programsUserDto", programsUserDto);

        return "exercises-log";
    }

    @GetMapping("/edit-exercise")
    public String showEditPage(@RequestParam String day,
                               @RequestParam Long id,
                               HttpSession session,
                               Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
        session.setAttribute("currentDay", day);

        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        ExerciseProgressDto foundDto = null;

        if (weekDays != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equalsIgnoreCase(day)) {
                    foundDto = dw.getExercises().stream()
                            .filter(ep -> ep.getId().equals(id))
                            .findFirst()
                            .orElse(null);
                }
            }
        }

        if (foundDto == null) return "redirect:/create";

        model.addAttribute("dto", foundDto);
        model.addAttribute("exercise", exerciseService.getById(foundDto.getExerciseId()));
        model.addAttribute("userDto", userDto);
        model.addAttribute("programsUserDto", programsUserDto);
        return "set-exercise";
    }

    @PostMapping("/edit-exercise")
    public String editExercise(@ModelAttribute("dto") ExerciseProgressDto dto,
                               HttpSession session) {
        String day = (String) session.getAttribute("currentDay");
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");

        if (weekDays != null && day != null && dto.getId() != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equalsIgnoreCase(day)) {
                    for (ExerciseProgressDto existing : dw.getExercises()) {
                        if (existing.getId().equals(dto.getId())) {
                            existing.setWeight(dto.getWeight());
                            existing.setReps(dto.getReps());
                            existing.setSets(dto.getSets());
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
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
        ExerciseDto exerciseDto = exerciseService.getById(exerciseId);

        session.setAttribute("exercise", exerciseDto);
        model.addAttribute("exercise", exerciseDto);
        model.addAttribute("dto", new ExerciseProgressDto());
        model.addAttribute("userDto", userDto);
        model.addAttribute("programsUserDto", programsUserDto);
        return "set-exercise";
    }

    @PostMapping("/add-exercise")
    public String addExercise(@ModelAttribute("dto") ExerciseProgressDto dto,
                              HttpSession session) {
        ExerciseDto exerciseDto = (ExerciseDto) session.getAttribute("exercise");
        String currentDay = (String) session.getAttribute("currentDay");
        if (exerciseDto == null || currentDay == null) return "redirect:/create";

        ExerciseProgressDto entry = new ExerciseProgressDto(
                exerciseDto.getId(),
                exerciseDto.getName(),
                dto.getReps(),
                dto.getSets(),
                dto.getWeight()
        );

        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        for (DayWorkout dayWorkout : weekDays) {
            if (dayWorkout.getDay().equals(currentDay)) {
                dayWorkout.getExercises().add(entry);
                break;
            }
        }
        session.setAttribute("weekDays", weekDays);
        return "redirect:/create";
    }

    @PostMapping("/remove-exercise")
    public String removeExercise(@RequestParam("day") String day,
                                 @RequestParam("id") Long id,
                                 HttpSession session) {
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        if (weekDays != null) {
            weekDays.stream()
                    .filter(dw -> dw.getDay().equalsIgnoreCase(day))
                    .findFirst()
                    .ifPresent(dw -> dw.getExercises().removeIf(ep -> id.equals(ep.getId())));
        }
        session.setAttribute("weekDays", weekDays);
        return "redirect:/create";
    }

    @PostMapping("/create-full-workout")
    public String createFullWorkout(@ModelAttribute("programForm") CreatedProgramDto dto,
                                    HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        CreatedProgramDto sessionForm = (CreatedProgramDto) session.getAttribute("programForm");
        if (sessionForm != null && sessionForm.getImageUrl() != null) {
            dto.setImageUrl(sessionForm.getImageUrl());
        }

        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        dto.setWeekDays(weekDays != null ? weekDays : new ArrayList<>());

        ProgramsUserDto userSettings = userService.getById(userDto.getId(), ProgramsUserDto.class);
        programService.createProgram(dto, userSettings, userSettings.getMeasuringUnits(), userDto.getExperience());

        // CRITICAL: Total session wipe to prevent data leakage into the next program
        session.removeAttribute("weekDays");
        session.removeAttribute("programForm");
        session.removeAttribute("currentDay");
        session.removeAttribute("exercise");
        session.removeAttribute("programId");

        return "redirect:/home";
    }

    @PostMapping("/upload-program-image-session")
    @ResponseBody
    public String uploadImageToSession(@RequestParam("programImage") MultipartFile imageFile,
                                       HttpSession session) {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = fileService.saveFile(imageFile);
            CreatedProgramDto sessionProgramForm = (CreatedProgramDto) session.getAttribute("programForm");
            if (sessionProgramForm == null) sessionProgramForm = new CreatedProgramDto();

            sessionProgramForm.setImageUrl(imageUrl);
            session.setAttribute("programForm", sessionProgramForm);
            return imageUrl;
        }
        return "error";
    }

    @PostMapping("/update-program-session")
    @ResponseBody
    public void updateProgramSession(@ModelAttribute CreatedProgramDto programFormInput,
                                     HttpSession session) {
        CreatedProgramDto sessionProgramForm = (CreatedProgramDto) session.getAttribute("programForm");
        if (sessionProgramForm == null) sessionProgramForm = new CreatedProgramDto();

        sessionProgramForm.setName(programFormInput.getName());
        sessionProgramForm.setScheduleMonths(programFormInput.getScheduleMonths());
        sessionProgramForm.setNotifications(programFormInput.getNotifications());
        sessionProgramForm.setIsPublic(programFormInput.getIsPublic());
        session.setAttribute("programForm", sessionProgramForm);
    }

    @PostMapping("/show/{id}")
    public String showExercise(@PathVariable Long id, HttpSession session, Model model){
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

    @PostMapping("/close-log")
    public String closeLog() {
        return "redirect:/create";
    }
}