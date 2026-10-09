package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ProfileUserDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.FileService;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Map;

@Controller
public class ProfileController {

    private final UserService userService;
    private final FileService fileService;
    private final LocaleResolver localeResolver;

    @Autowired
    public ProfileController(UserService userService,
                             FileService fileService,
                             LocaleResolver localeResolver) {
        this.userService = userService;
        this.fileService = fileService;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/profile/{id}")
    public String viewPublicProfile(@PathVariable Long id, Model model, HttpSession session) {
        // 1. Get current user from session just to handle theme/language context
        UserDto viewer = (UserDto) session.getAttribute("loggedUser");

        // 2. Fetch the profile data for the target ID
        // We use false for convertToLbs here unless you want to detect viewer's preference
        ProfileUserDto targetProfile = userService.getById(id, ProfileUserDto.class, false);
        System.out.println(targetProfile);

        // 3. Optional: Logic to check if I am looking at MY OWN profile
        boolean isOwnProfile = (viewer != null && viewer.getId().equals(id));
        if (isOwnProfile) {
            return "redirect:/profile"; // Send them to the editable version
        }

        model.addAttribute("profile", targetProfile);
        return "profile-view"; // A new, read-only HTML file
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) {
            return "redirect:/login";
        }

        // Fetch profile data, converting weight to Lbs if necessary
        boolean isLbs = "lbs".equals(userDto.getMeasuringUnits());
        ProfileUserDto profileData = userService.getById(userDto.getId(), ProfileUserDto.class, isLbs);

        model.addAttribute("profileForm", profileData);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("profileForm") ProfileUserDto profileDto,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam Map<String, String> settings,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {

        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) {
            return "redirect:/login";
        }

// 1. Handle Image Logic
        ProfileUserDto existing = userService.getById(
                profileDto.getId(), ProfileUserDto.class
        );

        String oldImageUrl = existing.getProfileImageUrl();
        String newImageUrl = null;

        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                newImageUrl = fileService.saveFile(profileImage);
                profileDto.setProfileImageUrl(newImageUrl);
            } else {
                profileDto.setProfileImageUrl(oldImageUrl);
            }

            // 2. Persist User Data
            userService.updateProfile(profileDto);

        } catch (RuntimeException e) {
            // Remove newly uploaded image if saving the profile fails
            if (newImageUrl != null) {
                fileService.deleteFile(newImageUrl);
            }
            throw e;
        }

// Delete the old image only after the profile was saved
        if (newImageUrl != null) {
            fileService.deleteFile(oldImageUrl);
        }

        // 3. Update Session & UI Settings (Language, Theme, Units)
        UserDto updatedUser = userService.getById(profileDto.getId(), UserDto.class);
        session.setAttribute("loggedUser", updatedUser);

        updateUserSettings(settings, session, request, response);

        return "redirect:/profile";
    }

    /**
     * Helper to synchronize UI preferences between the DB and the current Session/Locale.
     */
    private void updateUserSettings(Map<String, String> settings,
                                    HttpSession session,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        // Language Management
        if (settings.containsKey("language")) {
            String language = settings.get("language");
            if (!language.matches("en|bg|es|fr")) language = "en";

            session.setAttribute("language", language);
            localeResolver.setLocale(request, response, new Locale(language));
        }

        // Theme Management (light/dark)
        if (settings.containsKey("theme")) {
            String theme = settings.get("theme");
            if (!theme.matches("light|dark")) theme = "dark";
            session.setAttribute("theme", theme);
        }

        // Units Management (kg/lbs)
        if (settings.containsKey("units")) {
            String units = settings.get("units");
            if (!units.matches("kg|lbs")) units = "kg";
            session.setAttribute("units", units);
        }
    }
}