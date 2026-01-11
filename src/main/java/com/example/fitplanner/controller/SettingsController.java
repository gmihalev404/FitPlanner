package com.example.fitplanner.controller;

import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserService userService;
    private final LocaleResolver localeResolver;

    public SettingsController(UserService userService, LocaleResolver localeResolver) {
        this.userService = userService;
        this.localeResolver = localeResolver;
    }

    @GetMapping
    public String getSettings(HttpSession session, Model model) {
        return "settings";
    }

    @PostMapping("/language")
    public String changeLanguage(@RequestParam String language,
                                 HttpSession session,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        if (!language.matches("en|bg|es|fr")) language = "en";
        session.setAttribute("language", language);
        localeResolver.setLocale(request, response, new Locale(language));

        return "redirect:/settings";
    }

    @PostMapping("/theme")
    public String changeTheme(@RequestParam String theme, HttpSession session) {
        if (!theme.matches("light|dark")) theme = "dark";
        session.setAttribute("theme", theme);
        return "redirect:/settings";
    }

    @PostMapping("/units")
    public String changeUnits(@RequestParam String units, HttpSession session) {
        if (!units.matches("kg|lb")) units = "kg";
        session.setAttribute("units", units);
        return "redirect:/settings";
    }
}