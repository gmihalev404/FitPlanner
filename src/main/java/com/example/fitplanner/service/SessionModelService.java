package com.example.fitplanner.service;

import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.util.Settings;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class SessionModelService {

    public void populateModel(HttpSession session, Model model) {
        UserDto userDto = (UserDto) session.getAttribute("loggedUser");
        model.addAttribute("userDto", userDto);
        if(userDto == null) {
            model.addAttribute("theme", session.getAttribute("theme") != null ? session.getAttribute("theme") : Settings.DEFAULT_THEME);
            model.addAttribute("language", session.getAttribute("language") != null ? session.getAttribute("language") : Settings.DEFAULT_LANGUAGE);
            model.addAttribute("units", session.getAttribute("units") != null ? session.getAttribute("units") : Settings.DEFAULT_UNITS);
        }
        else{
            model.addAttribute("theme", userDto.getTheme());
            model.addAttribute("language", userDto.getLanguage());
            model.addAttribute("units", userDto.getMeasuringUnits());
        }
    }

    public void clearSession(HttpSession session) {
        session.removeAttribute("exercise-list");
        session.removeAttribute("weekDays");
        session.removeAttribute("currentDay");
        session.removeAttribute("exercise");
    }
}