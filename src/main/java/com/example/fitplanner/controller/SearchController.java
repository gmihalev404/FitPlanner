package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ForkableProgramDto;
import com.example.fitplanner.dto.TrainerSearchDto;
import com.example.fitplanner.entity.model.Program;
import com.example.fitplanner.entity.model.User;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.ProgramService;
import com.example.fitplanner.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final ProgramService programService;
    private final UserService userService;

    public SearchController(ProgramService programService, UserService userService) {
        this.programService = programService;
        this.userService = userService;
    }

    @GetMapping("/search")
    public String executeSearch(@RequestParam(value = "query", required = false) String query,
                                Model model) {
        String searchTerms = (query == null) ? "" : query.trim();

        // Change these to use the DTO mapping methods
        List<ForkableProgramDto> programs = programService.searchProgramsAndMap(searchTerms);
        // You'll need to create a similar method for Trainers
        List<TrainerSearchDto> trainers = userService.searchTrainersAndMap(searchTerms);

        model.addAttribute("programs", programs);
        model.addAttribute("trainers", trainers);
        model.addAttribute("query", searchTerms);

        return "search";
    }
}
