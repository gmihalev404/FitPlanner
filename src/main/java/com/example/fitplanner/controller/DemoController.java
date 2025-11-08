package com.example.fitplanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {
    @GetMapping("/register")
    public String register(){
        return "register-form";
    }
}