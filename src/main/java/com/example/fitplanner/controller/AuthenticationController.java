package com.example.fitplanner.controller;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.dto.UserLoginDto;
import com.example.fitplanner.dto.UserRegisterDto;
import com.example.fitplanner.dto.ProgramsUserDto;
import com.example.fitplanner.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
public class AuthenticationController {
    private final UserService userService;
    private final LocaleResolver localeResolver;
    @Autowired
    public AuthenticationController(UserService userService, LocaleResolver localeResolver) {
        this.userService = userService;
        this.localeResolver = localeResolver;
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session, Model model) {
        model.addAttribute("registerDto", new UserRegisterDto());
        return "register-form";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("registerDto") UserRegisterDto registerDto,
            BindingResult bindingResult,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {
        userService.validateUserRegister(registerDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return "register-form";
        }
        userService.save(registerDto);
        Long userId = userService.getIdByUsernameOrEmail(registerDto.getUsername());
        giveSession(userId, session, request, response);
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String showLoginForm(HttpSession session, Model model) {
        model.addAttribute("loginDto", new UserLoginDto());
        return "login-form";
    }

    @PostMapping("/login")
    public String handleLogin(
            @Valid @ModelAttribute("loginDto") UserLoginDto loginDto,
            BindingResult bindingResult,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {
        userService.validateUserLogin(loginDto, bindingResult);
        if (bindingResult.hasErrors()) {
            return "login-form";
        }
        Long userId = userService.getIdByUsernameOrEmail(loginDto.getUsernameOrEmail());
        giveSession(userId, session, request, response);
        return "redirect:/home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        return "redirect:/login";
    }

    private void giveSession(Long userId,
                             HttpSession session,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        UserDto userDto = userService.getById(userId, UserDto.class);
        session.setAttribute("loggedUser", userDto);
        localeResolver.setLocale(request, response, new Locale(userDto.getLanguage()));
    }
}