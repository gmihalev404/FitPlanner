package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
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
                                     ExerciseService exerciseService, FileService fileService) {
        this.userService = userService;
        this.programService = programService;
        this.exerciseService = exerciseService;
        this.fileService = fileService;
    }

    @GetMapping("/create")
    public String createWorkout(HttpSession session, Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";
        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
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
        if (weekDays == null || weekDays.isEmpty()) {
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
        Long programId = (Long) session.getAttribute("programId");
        model.addAttribute("programId", programId);
        model.addAttribute("userDto", userDto);
        model.addAttribute("programsUserDto", programsUserDto);
        return "create";
    }

    @GetMapping("/exercise-log")
    public String showExerciseLog(@RequestParam(required = false) String day,
                                  HttpSession session,
                                  Model model){
        UserDto sessionUser = (UserDto) session.getAttribute("loggedUser");
        if (sessionUser == null) return "redirect:/login";

        // FETCH FRESH DATA: This ensures notifications and roles are not null/stale
        UserDto userDto = userService.getById(sessionUser.getId(), UserDto.class);
        ProgramsUserDto programsUserDto = userService.getById(sessionUser.getId(), ProgramsUserDto.class);

        if(day != null) session.setAttribute("currentDay", day);

        List<ExerciseDto> exercises = exerciseService.getAll();

        // Add to model
        model.addAttribute("exercises", exercises);
        model.addAttribute("userDto", userDto); // Fresh data for Navbar
        model.addAttribute("programsUserDto", programsUserDto);

        return "exercises-log"; // Verify this matches your file name exactly
    }

    @GetMapping("/edit-exercise")
    public String showEditPage(@RequestParam String day,
                               @RequestParam Long id,
                               HttpSession session,
                               Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if(userDto == null) return "redirect:/login";
        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
        session.setAttribute("currentDay", day);
        session.setAttribute("exerciseId", id);
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        ExerciseProgressDto foundDto = null;
        if (weekDays != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equalsIgnoreCase(day)) {
                    for (ExerciseProgressDto ep : dw.getExercises()) {
                        if (ep.getId().equals(id)) {
                            foundDto = ep;
                            break;
                        }
                    }
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
                    break;
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
        session.removeAttribute("exercise");
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
        if (weekDays != null && day != null && id != null) {
            for (DayWorkout dw : weekDays) {
                if (dw.getDay().equalsIgnoreCase(day)) {
                    dw.getExercises().removeIf(ep -> id.equals(ep.getId()));
                    break;
                }
            }
        }
        session.setAttribute("weekDays", weekDays);
        return "redirect:/create";
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

    @PostMapping("/create-full-workout")
    public String createFullWorkout(@ModelAttribute("programForm") CreatedProgramDto dto,
                                    HttpSession session) {

        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        // 1. Get the form from session (which now contains our Image URL)
        CreatedProgramDto sessionForm = (CreatedProgramDto) session.getAttribute("programForm");

        // 2. Merge data (Manual merge or use the session version as base)
        if (sessionForm != null && sessionForm.getImageUrl() != null) {
            dto.setImageUrl(sessionForm.getImageUrl());
        }

        // 3. Get weekDays from session
        List<DayWorkout> weekDays = (List<DayWorkout>) session.getAttribute("weekDays");
        dto.setWeekDays(weekDays != null ? weekDays : new ArrayList<>());

        ProgramsUserDto userSettings = userService.getById(userDto.getId(), ProgramsUserDto.class);
        programService.createProgram(dto, userSettings, userSettings.getMeasuringUnits(), userDto.getExperience());

        // 4. Full Cleanup
        session.removeAttribute("weekDays");
        session.removeAttribute("programForm");

        return "redirect:/home";
    }

    @PostMapping("/upload-program-image-session")
    @ResponseBody
    public String uploadImageToSession(@RequestParam("programImage") MultipartFile imageFile,
                                       HttpSession session) {
        if (imageFile != null && !imageFile.isEmpty()) {
            // 1. Save file to disk immediately
            String imageUrl = fileService.saveImage(imageFile);

            // 2. Store the string path in the session form
            CreatedProgramDto sessionProgramForm = (CreatedProgramDto) session.getAttribute("programForm");
            if (sessionProgramForm == null) {
                sessionProgramForm = new CreatedProgramDto();
            }
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
        if (sessionProgramForm == null) {
            sessionProgramForm = new CreatedProgramDto();
        }
        sessionProgramForm.setName(programFormInput.getName());
        sessionProgramForm.setScheduleMonths(programFormInput.getScheduleMonths());
        sessionProgramForm.setNotifications(programFormInput.getNotifications());
        sessionProgramForm.setIsPublic(programFormInput.getIsPublic());
        session.setAttribute("programForm", sessionProgramForm);
    }
}