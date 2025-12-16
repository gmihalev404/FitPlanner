package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserLoginDto;
import com.example.fitplanner.dto.UserRegisterDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthenticationController {
    private final UserService userService;

    @Autowired
    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session, Model model) {
        model.addAttribute("registerDto", new UserRegisterDto());
        addThemeAndLanguage(session, model);
        return "register-form";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("registerDto") UserRegisterDto registerDto,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        userService.validateUserRegister(registerDto, bindingResult);
        if (bindingResult.hasErrors()) {
            addThemeAndLanguage(session, model);
            return "register-form";
        }
        userService.save(registerDto);
        Long userId = userService.getIdByUsernameOrEmail(registerDto.getUsername());
        UserDto userDto = userService.getById(userId);
        session.setAttribute("loggedInUser", userDto);
        session.setAttribute("theme", userDto.getTheme());
        session.setAttribute("language", userDto.getLanguage());
        return "redirect:/home/" + userId;
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        model.addAttribute("loginDto", new UserLoginDto());
        addThemeAndLanguage(session, model);
        return "login-form";
    }

    @PostMapping("/login")
    public String handleLogin(
            @Valid @ModelAttribute("loginDto") UserLoginDto loginDto,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        userService.validateUserLogin(loginDto, bindingResult);
        if (bindingResult.hasErrors()) {
            addThemeAndLanguage(session, model);
            return "login-form";
        }
        Long userId = userService.getIdByUsernameOrEmail(loginDto.getUsernameOrEmail());
        UserDto userDto = userService.getById(userId);
        session.setAttribute("loggedInUser", userDto);
        session.setAttribute("theme", userDto.getTheme());
        session.setAttribute("language", userDto.getLanguage());
        return "redirect:/home/" + userId;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private void addThemeAndLanguage(HttpSession session, Model model) {
        String theme = (String) session.getAttribute("theme");
        String language = (String) session.getAttribute("language");
        model.addAttribute("theme", theme != null ? theme : "dark");
        model.addAttribute("language", language != null ? language : "en");
    }
}