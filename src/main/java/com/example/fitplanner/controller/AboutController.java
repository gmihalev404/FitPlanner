package com.example.fitplanner.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String DEFAULT_THEME = "dark";

    @GetMapping("/about")
    public String getAbout(HttpSession session, Model model) {
        addThemeAndLanguage(session, model);
        return "about";
    }

    private void addThemeAndLanguage(HttpSession session, Model model) {
        String theme = (String) session.getAttribute("theme");
        String language = (String) session.getAttribute("language");
        model.addAttribute("theme", theme != null ? theme : DEFAULT_THEME);
        model.addAttribute("language", language != null ? language : DEFAULT_LANGUAGE);
    }
}