package com.example.fitplanner.controller;

import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.*;
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
    public SettingsController(UserService userService,
                              LocaleResolver localeResolver) {
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
        localeResolver.setLocale(request, response, new Locale(language));
        session.setAttribute("language", language);
        return "redirect:/settings";
    }

    @PostMapping("/theme")
    public String changeTheme(@RequestParam String theme,
                              HttpSession session,
                              HttpServletResponse response) {
        if (!theme.matches("light|dark")) theme = "dark";
        addCookie(response, "theme", theme);
        session.setAttribute("theme", theme);
        return "redirect:/settings";
    }

    @PostMapping("/units")
    public String changeUnits(@RequestParam String units,
                              HttpSession session,
                              HttpServletResponse response) {
        if (!units.matches("kg|lb")) units = "kg";
        addCookie(response, "units", units);
        session.setAttribute("units", units);
        return "redirect:/settings";
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }
}