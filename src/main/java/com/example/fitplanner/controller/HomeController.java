package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/home/{id}")
    public String getHomeById(@PathVariable Long id, HttpSession session, Model model) {
        UserDto userDto = userService.getById(id);
        if (userDto == null) return "redirect:/login";

        model.addAttribute("userDto", userDto);

        session.setAttribute("loggedInUser", userDto);
        session.setAttribute("theme", userDto.getTheme() != null ? userDto.getTheme() : "dark");
        session.setAttribute("language", userDto.getLanguage() != null ? userDto.getLanguage() : "en");

        model.addAttribute("theme", session.getAttribute("theme"));
        model.addAttribute("language", session.getAttribute("language"));

        return "home";
    }

    @GetMapping("/home")
    public String getHome(HttpSession session, Model model) {
        model.addAttribute("userDto", null);
        model.addAttribute("theme", session.getAttribute("theme") != null ? session.getAttribute("theme") : "dark");
        model.addAttribute("language", session.getAttribute("language") != null ? session.getAttribute("language") : "en");
        return "home";
    }

    @GetMapping("/")
    public String get(HttpSession session, Model model) {
        model.addAttribute("userDto", null);
        model.addAttribute("theme", session.getAttribute("theme") != null ? session.getAttribute("theme") : "dark");
        model.addAttribute("language", session.getAttribute("language") != null ? session.getAttribute("language") : "en");
        return "home";
    }
}