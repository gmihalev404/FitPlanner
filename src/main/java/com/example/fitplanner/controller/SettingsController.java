package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
public class SettingsController {

    private final UserService userService;
    private final LocaleResolver localeResolver;

    @Autowired
    public SettingsController(UserService userService, LocaleResolver localeResolver) {
        this.userService = userService;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/settings")
    public String getSettings(HttpSession session, Model model) {
        String theme = (String) session.getAttribute("theme");
        String language = (String) session.getAttribute("language");
        model.addAttribute("theme", theme != null ? theme : "dark");
        model.addAttribute("language", language != null ? language : "en");
        return "settings";
    }

    @PostMapping("/settings/language")
    public String changeLanguage(@RequestParam String language,
                                 HttpSession session,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        if (!language.matches("en|bg|es|fr")) language = "en";

        session.setAttribute("language", language);
        localeResolver.setLocale(request, response, new Locale(language));

        UserDto userDto = (UserDto) session.getAttribute("loggedInUser");
        if (userDto != null) {
            userDto.setLanguage(language);
            userService.updateUserSettings(userDto);
            session.setAttribute("loggedInUser", userDto);
        }

        return "redirect:/settings";
    }

    @PostMapping("/settings/theme")
    public String changeTheme(@RequestParam String theme, HttpSession session) {
        if (!theme.matches("light|dark")) theme = "dark";

        session.setAttribute("theme", theme);

        UserDto userDto = (UserDto) session.getAttribute("loggedInUser");
        if (userDto != null) {
            userDto.setTheme(theme);
            userService.updateUserSettings(userDto);
            session.setAttribute("loggedInUser", userDto);
        }

        return "redirect:/settings";
    }
}