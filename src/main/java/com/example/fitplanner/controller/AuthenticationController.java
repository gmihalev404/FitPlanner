package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserLoginDto;
import com.example.fitplanner.dto.UserRegisterDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.SessionModelService;
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
    private final SessionModelService sessionModelService;

    @Autowired
    public AuthenticationController(UserService userService, SessionModelService sessionModelService) {
        this.userService = userService;
        this.sessionModelService = sessionModelService;
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session, Model model) {
        model.addAttribute("registerDto", new UserRegisterDto());
        sessionModelService.populateModel(session, model);
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
            sessionModelService.populateModel(session, model);
            return "register-form";
        }
        userService.save(registerDto);
        Long userId = userService.getIdByUsernameOrEmail(registerDto.getUsername());
        UserDto userDto = userService.getById(userId);
        session.setAttribute("loggedUser", userDto);
        sessionModelService.populateModel(session, model);
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        model.addAttribute("loginDto", new UserLoginDto());
        sessionModelService.populateModel(session, model);
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
            sessionModelService.populateModel(session, model);
            return "login-form";
        }
        Long userId = userService.getIdByUsernameOrEmail(loginDto.getUsernameOrEmail());
        UserDto userDto = userService.getById(userId);
        session.setAttribute("loggedUser", userDto);
        sessionModelService.populateModel(session, model);
        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}