package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ExerciseDto;
import com.example.fitplanner.dto.QuoteDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.entity.enums.Category;
import com.example.fitplanner.entity.enums.EquipmentType;
import com.example.fitplanner.entity.enums.ExerciseType;
import com.example.fitplanner.service.ActivityService;
import com.example.fitplanner.service.ExerciseService;
import com.example.fitplanner.service.QuoteService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ExerciseService exerciseService;
    private final QuoteService quoteService;
    private final ActivityService activityService;
    private final UserService userService;

    public AdminController(ExerciseService exerciseService, QuoteService quoteService, ActivityService activityService, UserService userService) {
        this.exerciseService = exerciseService;
        this.quoteService = quoteService;
        this.activityService = activityService;
        this.userService = userService;
    }

    @GetMapping("/management")
    public String adminManagement(Model model, HttpSession session) {
        // 1. Check session BEFORE anything else
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");

        if (userDto == null || !userDto.getRole().name().equals("ADMIN")) {
            return "redirect:/users/login";
        }

        // 2. Add the User
        model.addAttribute("userDto", userDto);

        // 3. Add the Lists (Ensure they aren't null)
        model.addAttribute("exercises", exerciseService.getAll());
        model.addAttribute("quotes", quoteService.getAll());

        // 4. IMPORTANT: Add the Enums for the dropdowns
        model.addAttribute("categories", Category.values());
        model.addAttribute("exerciseTypes", ExerciseType.values());
        model.addAttribute("equipmentTypes", EquipmentType.values());

        return "admin-panel";
    }

    @PostMapping("/add/exercise")
    public String addExercise(@RequestParam String name,
                              @RequestParam Category category,
                              @RequestParam ExerciseType exerciseType,
                              @RequestParam EquipmentType equipmentType) {

        ExerciseDto newExercise = new ExerciseDto();
        newExercise.setName(name);
        newExercise.setCategory(category);
        newExercise.setExerciseType(exerciseType);
        newExercise.setEquipmentType(equipmentType);

        exerciseService.create(newExercise);
        return "redirect:/management"; // Redirects back to the admin panel
    }

    @PostMapping("/edit/exercise/{id}")
    public String editExercise(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam Category category,
                               @RequestParam ExerciseType exerciseType,
                               @RequestParam EquipmentType equipmentType) {

        ExerciseDto updatedDto = new ExerciseDto();
        updatedDto.setName(name);
        updatedDto.setCategory(category);
        updatedDto.setExerciseType(exerciseType);
        updatedDto.setEquipmentType(equipmentType);

        exerciseService.update(id, updatedDto);
        return "redirect:/management";
    }

    @PostMapping("/exercises/delete/{id}")
    public String deleteExercise(@PathVariable Long id) {
        exerciseService.delete(id);
        return "redirect:/management";
    }

    @PostMapping("/add/quote")
    public String addQuote(@RequestParam String text,
                           @RequestParam(required = false) String author) {

        QuoteDto quoteDto = new QuoteDto();
        quoteDto.setText(text);
        // Default to 'Unknown' if author is empty
        quoteDto.setAuthor((author == null || author.isBlank()) ? "Unknown" : author);

        quoteService.create(quoteDto);
        return "redirect:/management";
    }

    @PostMapping("/edit/quote/{id}")
    public String editQuote(@PathVariable Long id,
                            @RequestParam String text,
                            @RequestParam String author) {

        QuoteDto quoteDto = new QuoteDto();
        quoteDto.setText(text);
        quoteDto.setAuthor(author);

        quoteService.update(id, quoteDto);
        return "redirect:/management";
    }

    @PostMapping("/quotes/delete/{id}")
    public String deleteQuote(@PathVariable Long id) {
        quoteService.delete(id);
        return "redirect:/management";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Safety: prevent admin from banning themselves
        userService.toggleStatus(id);
        return "redirect:/management";
    }
}