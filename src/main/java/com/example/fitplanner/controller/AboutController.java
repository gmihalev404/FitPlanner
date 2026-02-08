package com.example.fitplanner.controller;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.service.SessionModelService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {
    public AboutController() {
    }

    @GetMapping("/about")
    public String getAbout(HttpSession session, Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        if (!userDto.getEnabled()) {
            session.invalidate();
            return "redirect:/login?banned=true";
        }
        return "about";
    }
}