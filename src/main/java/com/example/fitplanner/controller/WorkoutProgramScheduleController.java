package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.UserService;
import com.example.fitplanner.service.WorkoutSessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class WorkoutProgramScheduleController {
    private final ProgramService programService;
    private final WorkoutSessionService workoutSessionService;
    private final UserService userService;

    @Autowired
    public WorkoutProgramScheduleController(ProgramService programService,
                                            WorkoutSessionService workoutSessionService, UserService userService) {
        this.programService = programService;
        this.workoutSessionService = workoutSessionService;
        this.userService = userService;
    }


    @GetMapping("my-workouts")
    public String showWorkouts(HttpSession session, Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";
        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
        List<ProgramDto> programDtos = programService.getProgramsByUserId(userDto.getId());
        model.addAttribute("programs", programDtos);
        return "my-workouts";
    }

    @GetMapping("session")
    public String showDateSession(@RequestParam Integer year,
                                  @RequestParam Integer month,
                                  @RequestParam Integer day,
                                  @RequestParam(required = false) List<Long> programIds,
                                  Model model, HttpSession session){
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if(userDto == null) return "redirect:/login";
        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
        LocalDate date = LocalDate.of(year, month, day);
        List<ExerciseProgressDto> exercises = workoutSessionService.getWorkoutsByProgramIdsAndDate(programIds, date,
                programsUserDto.getMeasuringUnits());
        model.addAttribute("exercises", exercises);
        model.addAttribute("date", date);
        model.addAttribute("isToday", LocalDate.now().equals(date));
        model.addAttribute("units", programsUserDto.getMeasuringUnits());
        return "workout-day";
    }

    @GetMapping("edit-program")
    public String showEditForm(@RequestParam Long programId, HttpSession session){
        CreatedProgramDto programDto = programService.getById(programId, CreatedProgramDto.class);
        session.setAttribute("programForm", programDto);
        session.setAttribute("weekDays", programDto.getWeekDays());
        session.setAttribute("programId", programId);
        return "redirect:/create";
    }

    @PostMapping("edit-program")
    public String editProgram(@ModelAttribute CreatedProgramDto createdProgramDto,
                              HttpSession session) {
        Long programId = (Long) session.getAttribute("programId");
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        ProgramsUserDto programsUserDto = userService.getById(userDto.getId(), ProgramsUserDto.class);
        List<DayWorkout> sessionDays = (List<DayWorkout>) session.getAttribute("weekDays");
        createdProgramDto.setWeekDays(sessionDays);

        programService.updateProgram(programId, createdProgramDto, programsUserDto.getMeasuringUnits());
        return "redirect:/my-workouts";
    }

    @PostMapping("/remove-program")
    public String removeProgram(@RequestParam Long programId) {
        programService.removeProgram(programId);
        return "redirect:/my-workouts";
    }

    @PostMapping("/close-day-session")
    public String closeDaySession() {
        return "redirect:/my-workouts";
    }
}