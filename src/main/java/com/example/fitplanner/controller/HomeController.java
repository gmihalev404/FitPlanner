package com.example.fitplanner.controller;

import com.example.fitplanner.service.SessionModelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final SessionModelService sessionModelService;

    public HomeController(SessionModelService sessionModelService) {
        this.sessionModelService = sessionModelService;
    }

    @GetMapping("/home")
    public String getHome(HttpSession session, Model model) {
        sessionModelService.populateModel(session, model);
        sessionModelService.clearSession(session);
        session.removeAttribute("programForm");
        if (model.getAttribute("userDto") == null) return "redirect:/login";
        return "home";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }
}