package com.example.fitplanner.controller;

import com.example.fitplanner.dto.CreatedProgramDto;
import com.example.fitplanner.dto.ProgramDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.SessionModelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WorkoutProgramScheduleController {

    private final SessionModelService sessionModelService;
    private final ProgramService programService;

    @Autowired
    public WorkoutProgramScheduleController(SessionModelService sessionModelService, ProgramService programService) {
        this.sessionModelService = sessionModelService;
        this.programService = programService;
    }


    @GetMapping("my-workouts")
    public String showWorkouts(HttpSession session, Model model) {
        UserDto sessionUser = (UserDto) session.getAttribute("loggedUser");
        if (sessionUser == null) return "redirect:/login";
        sessionModelService.populateModel(session, model);
        List<ProgramDto> programDtos = programService.getProgramsByUserId(sessionUser.getId());
        for (ProgramDto programDto : programDtos) {
            System.out.println("here");
            System.out.println(programDto);
        }
        model.addAttribute("programs", programDtos);
        return "my-workouts";
    }
}