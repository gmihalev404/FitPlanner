package com.example.fitplanner.service;

import com.example.fitplanner.dto.ProgramsUserDto;
import com.example.fitplanner.dto.UserDto;
import com.example.fitplanner.util.Settings;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class SessionModelService {

    private final Set<HttpSession> sessions = Collections.synchronizedSet(new HashSet<>());

    // Add a session when user logs in or first accesses app
    public void registerSession(HttpSession session) {
        sessions.add(session);
    }

    // Remove session when user logs out
    public void unregisterSession(HttpSession session) {
        sessions.remove(session);
    }

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
//            model.addAttribute("units", userDto.getMeasuringUnits());
        }
    }

    public void clearProgramCreationSession(HttpSession session) {
        session.removeAttribute("exercise-list");
        session.removeAttribute("weekDays");
        session.removeAttribute("currentDay");
        session.removeAttribute("exercise");
        session.removeAttribute("programForm");
        session.removeAttribute("programId");
    }


    public void clearSession(HttpSession session) {
        session.invalidate();
        unregisterSession(session);
    }

    public void clearAllSessions() {
        synchronized (sessions) {
            for (HttpSession session : sessions) {
                try {
                    session.invalidate();
                } catch (IllegalStateException ignored) {
                    // already invalidated
                }
            }
            sessions.clear();
        }
    }
}