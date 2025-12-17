package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.SessionModelService;
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
    private final SessionModelService sessionModelService;

    public SettingsController(UserService userService,
                              LocaleResolver localeResolver,
                              SessionModelService sessionModelService) {
        this.userService = userService;
        this.localeResolver = localeResolver;
        this.sessionModelService = sessionModelService;
    }

    @GetMapping
    public String getSettings(HttpSession session, Model model) {
        sessionModelService.populateModel(session, model);
        return "settings";
    }

    @PostMapping("/language")
    public String changeLanguage(@RequestParam String language,
                                 HttpSession session,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto != null) {
            if (!language.matches("en|bg|es|fr")) language = "en";
            userDto.setLanguage(language);
            userService.updateUserSettings(userDto);
            session.setAttribute("loggedUser", userDto);
            session.setAttribute("language", language);
            localeResolver.setLocale(request, response, new Locale(language));
        }
        return "redirect:/settings";
    }

    @PostMapping("/theme")
    public String changeTheme(@RequestParam String theme, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto != null) {
            if (!theme.matches("light|dark")) theme = "dark";
            userDto.setTheme(theme);
            userService.updateUserSettings(userDto);
            session.setAttribute("loggedUser", userDto);
            session.setAttribute("theme", theme);
        }
        return "redirect:/settings";
    }

    @PostMapping("/units")
    public String changeUnits(@RequestParam String units, HttpSession session) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (userDto != null) {
            if (!units.matches("kg|lb")) units = "kg";
            userDto.setMeasuringUnits(units);
            userService.updateUserSettings(userDto);
            session.setAttribute("loggedUser", userDto);
            session.setAttribute("units", units);
        }
        return "redirect:/settings";
    }
}