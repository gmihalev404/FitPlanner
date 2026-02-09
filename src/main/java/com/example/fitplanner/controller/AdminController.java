package com.example.fitplanner.controller;

import com.example.fitplanner.dto.*;
import com.example.fitplanner.entity.enums.*;
import com.example.fitplanner.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ExerciseService exerciseService;
    private final QuoteService quoteService;
    private final UserService userService;
    private final FileService fileService;

    public AdminController(ExerciseService exerciseService, QuoteService quoteService,
                           UserService userService, FileService fileService) {
        this.exerciseService = exerciseService;
        this.quoteService = quoteService;
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping("/management")
    public String adminManagement(Model model, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null || !userDto.getRole().name().equals("ADMIN")) {
            return "redirect:/users/login";
        }
        model.addAttribute("userDto", userDto);
        model.addAttribute("exercises", exerciseService.getAll());
        model.addAttribute("quotes", quoteService.getAll());
        model.addAttribute("allUsers", userService.getAllUsers());

        model.addAttribute("categories", Category.values());
        model.addAttribute("exerciseTypes", ExerciseType.values());
        model.addAttribute("equipmentTypes", EquipmentType.values());
        return "admin-panel";
    }

    // --- EXERCISES ---

    @PostMapping("/exercises/add")
    public String addExercise(@ModelAttribute ExerciseDto exerciseDto,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              @RequestParam("videoFile") MultipartFile videoFile) {
        exerciseDto.setImageUrl(fileService.saveFile(imageFile));
        exerciseDto.setVideoUrl(fileService.saveFile(videoFile));
        exerciseService.create(exerciseDto);
        return "redirect:/admin/management";
    }

    @PostMapping("/exercises/edit/{id}")
    public String editExercise(@PathVariable Long id,
                               @ModelAttribute ExerciseDto exerciseDto,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               @RequestParam("videoFile") MultipartFile videoFile) {
        ExerciseDto existing = exerciseService.getById(id);
        if (existing == null) return "redirect:/admin/management";

        exerciseDto.setId(id);

        if (!imageFile.isEmpty()) {
            fileService.deleteFile(existing.getImageUrl());
            exerciseDto.setImageUrl(fileService.saveFile(imageFile));
        } else {
            exerciseDto.setImageUrl(existing.getImageUrl());
        }

        if (!videoFile.isEmpty()) {
            fileService.deleteFile(existing.getVideoUrl());
            exerciseDto.setVideoUrl(fileService.saveFile(videoFile));
        } else {
            exerciseDto.setVideoUrl(existing.getVideoUrl());
        }

        exerciseDto.setGetLastCompleted(existing.getGetLastCompleted());
        exerciseService.update(id, exerciseDto);
        return "redirect:/admin/management";
    }

    @PostMapping("/exercises/delete/{id}")
    public String deleteExercise(@PathVariable Long id) {
        ExerciseDto ex = exerciseService.getById(id);
        if (ex != null) {
            fileService.deleteFile(ex.getImageUrl());
            fileService.deleteFile(ex.getVideoUrl());
        }
        exerciseService.delete(id);
        return "redirect:/admin/management";
    }

    // --- QUOTES ---

    @PostMapping("/quotes/add")
    public String addQuote(@ModelAttribute QuoteDto quoteDto) {
        quoteService.create(quoteDto);
        return "redirect:/admin/management";
    }

    @PostMapping("/quotes/edit/{id}")
    public String editQuote(@PathVariable Long id, @ModelAttribute QuoteDto quoteDto) {
        quoteService.update(id, quoteDto);
        return "redirect:/admin/management";
    }

    @PostMapping("/quotes/delete/{id}")
    public String deleteQuote(@PathVariable Long id) {
        quoteService.delete(id);
        return "redirect:/admin/management";
    }

    // --- USERS ---

    @GetMapping("/users/profile/{id}")
    public String viewDetailedProfile(@PathVariable Long id, Model model, HttpSession session) {
        UserDto loggedUser = (UserDto) session.getAttribute("loggedUser");
        if (loggedUser == null || !loggedUser.getRole().name().equals("ADMIN")) return "redirect:/users/login";

        DetailedUserDto detailedUser = userService.getDetailedUserById(id);
        if (detailedUser == null) return "redirect:/admin/management";

        model.addAttribute("userDto", loggedUser);
        model.addAttribute("targetUser", detailedUser);
        return "admin-user-details";
    }

    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id) {
        userService.toggleStatus(id);
        return "redirect:/admin/management";
    }
}