package com.example.fitplanner.controller;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.SessionModelService;
import com.example.fitplanner.util.Settings;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {

    private final SessionModelService sessionModelService;

    @Autowired
    public AboutController(SessionModelService sessionModelService) {
        this.sessionModelService = sessionModelService;
    }

    @GetMapping("/about")
    public String getAbout(HttpSession session, Model model) {
        sessionModelService.populateModel(session, model);
        return "about";
    }
}