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
    public String getSettings(HttpSession session,
                              Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedInUser");
        String theme = (String) session.getAttribute("theme");
        String language = (String) session.getAttribute("language");
        String units = (String) session.getAttribute("units");
        model.addAttribute("theme", theme != null ? theme : "dark");
        model.addAttribute("language", language != null ? language : "en");
        model.addAttribute("units", units != null ? units : "kg");
        model.addAttribute("userDto", userDto);
        return "settings";
    }

    @GetMapping("/settings/{id}")
    public String getSettingsById(@PathVariable Long id,
                                  HttpSession session,
                                  Model model) {
        UserDto userDto = userService.getById(id);
        if (userDto != null) {
            session.setAttribute("theme", userDto.getTheme() != null ? userDto.getTheme() : "dark");
            session.setAttribute("language", userDto.getLanguage() != null ? userDto.getLanguage() : "en");
            session.setAttribute("units", userDto.getMeasuringUnits() != null ? userDto.getMeasuringUnits() : "kg");
        } else {
            session.setAttribute("theme", "dark");
            session.setAttribute("language", "en");
            session.setAttribute("units", "kg");
        }
        session.setAttribute("loggedInUser", userDto);
        model.addAttribute("theme", session.getAttribute("theme"));
        model.addAttribute("language", session.getAttribute("language"));
        model.addAttribute("units", session.getAttribute("units"));
        model.addAttribute("userDto", userDto);
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

    @PostMapping("/settings/language/{id}")
    public String changeLanguageById(@PathVariable Long id,
                                     @RequestParam String language,
                                     HttpSession session,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        if (!language.matches("en|bg|es|fr")) language = "en";
        UserDto userDto = userService.getById(id);
        if (userDto != null) {
            userDto.setLanguage(language);
            userService.updateUserSettings(userDto);
            UserDto currentUser = (UserDto) session.getAttribute("loggedInUser");
            if (currentUser != null && currentUser.getId().equals(id)) {
                session.setAttribute("language", language);
                session.setAttribute("loggedInUser", userDto);
                localeResolver.setLocale(request, response, new Locale(language));
            }
        }
        session.setAttribute("loggedInUser", userDto);
        return "redirect:/settings";
    }

    @PostMapping("/settings/theme")
    public String changeTheme(@RequestParam String theme,
                              HttpSession session) {
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

    @PostMapping("/settings/theme/{id}")
    public String changeThemeById(@PathVariable Long id,
                                  @RequestParam String theme,
                                  HttpSession session) {
        if (!theme.matches("light|dark")) theme = "dark";
        UserDto userDto = userService.getById(id);
        if (userDto != null) {
            userDto.setTheme(theme);
            userService.updateUserSettings(userDto);
            UserDto currentUser = (UserDto) session.getAttribute("loggedInUser");
            if (currentUser != null && currentUser.getId().equals(id)) {
                session.setAttribute("theme", theme);
                session.setAttribute("loggedInUser", userDto);
            }
        }
        session.setAttribute("loggedInUser", userDto);
        return "redirect:/settings";
    }

    @PostMapping("/settings/units")
    public String changeUnits(@RequestParam String units,
                              HttpSession session) {
        if (!units.matches("kg|lb")) units = "kg";
        session.setAttribute("units", units);
        UserDto userDto = (UserDto) session.getAttribute("loggedInUser");
        if (userDto != null) {
            userDto.setMeasuringUnits(units);
            userService.updateUserSettings(userDto);
            session.setAttribute("loggedInUser", userDto);
        }
        return "redirect:/settings";
    }

    @PostMapping("/settings/units/{id}")
    public String changeUnitsById(@PathVariable Long id,
                                  @RequestParam String units,
                                  HttpSession session) {
        if (!units.matches("kg|lb")) units = "kg";
        UserDto userDto = userService.getById(id);
        if (userDto != null) {
            userDto.setMeasuringUnits(units);
            userService.updateUserSettings(userDto);
            UserDto currentUser = (UserDto) session.getAttribute("loggedInUser");
            if (currentUser != null && currentUser.getId().equals(id)) {
                session.setAttribute("units", units);
                session.setAttribute("loggedInUser", userDto);
            }
        }
        session.setAttribute("loggedInUser", userDto);
        return "redirect:/settings";
    }
}