package com.example.fitplanner.controller;

import com.example.fitplanner.dto.ProfileUserDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;

@Controller
public class ProfileController {

    private final UserService userService;
    private final LocaleResolver localeResolver;

    @Autowired
    public ProfileController(UserService userService, LocaleResolver localeResolver) {
        this.userService = userService;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto == null) return "redirect:/login";

        ProfileUserDto profileData = userService.getById(userDto.getId(), ProfileUserDto.class);
        model.addAttribute("profileForm", profileData);
        return "/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute("profileForm") ProfileUserDto profileDto,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam Map<String, String> settings,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UserDto loggedUser = (UserDto) session.getAttribute("loggedUser");
        if (loggedUser == null) return "redirect:/login";

        ProfileUserDto existing = userService.getById(profileDto.getId(), ProfileUserDto.class);

        // Upload profile image if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImageUrl = uploadImage(profileImage);
            profileDto.setProfileImageUrl(profileImageUrl);
        } else {
            profileDto.setProfileImageUrl(existing.getProfileImageUrl());
        }

        // Update DB
        userService.updateProfile(profileDto);

        // Update session user
        session.setAttribute("loggedUser", userService.getById(profileDto.getId(), UserDto.class));

        // Update session-based settings
        updateUserSettings(settings, session, request, response);

        return "redirect:/profile";
    }

    private String uploadImage(MultipartFile profileImage) {
        String uploadDir = "uploads/";
        String fileName = System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(profileImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + fileName;
        } catch (IOException | MaxUploadSizeExceededException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUserSettings(Map<String, String> settings,
                                    HttpSession session,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        // Language
        if (settings.containsKey("language")) {
            String language = settings.get("language");
            if (!language.matches("en|bg|es|fr")) language = "en";

            session.setAttribute("language", language);
            localeResolver.setLocale(request, response, new Locale(language));
        }

        // Theme
        if (settings.containsKey("theme")) {
            String theme = settings.get("theme");
            if (!theme.matches("light|dark")) theme = "dark";
            session.setAttribute("theme", theme);
        }

        // Units
        if (settings.containsKey("units")) {
            String units = settings.get("units");
            if (!units.matches("kg|lb")) units = "kg";
            session.setAttribute("units", units);
        }
    }
}