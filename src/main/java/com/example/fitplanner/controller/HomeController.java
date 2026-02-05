package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    public HomeController() {
    }

    @GetMapping("/home")
    public String getHome(HttpSession session, Model model) {
        session.removeAttribute("programForm");
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        model.addAttribute("userDto", userDto);
        session.removeAttribute("sessionExercises");
//        if (model.getAttribute("userDto") == null) return "redirect:/login";
        return "home";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/home";
    }
}