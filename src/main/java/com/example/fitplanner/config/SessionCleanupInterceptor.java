package com.example.fitplanner.config;

import com.example.fitplanner.service.SessionModelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class SessionCleanupInterceptor implements HandlerInterceptor {

    private final SessionModelService sessionModelService;

    public SessionCleanupInterceptor(SessionModelService sessionModelService) {
        this.sessionModelService = sessionModelService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String uri = request.getRequestURI();

        // skip static resources
        if (uri.contains(".") || uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/webjars")) {
            return true;
        }

        // workflow pages that should keep session
        List<String> safeZone = List.of(
                "/create", "/exercise-log", "/edit-exercise", "/set-exercise",
                "/add-exercise", "/remove-exercise", "/show", "/update-program-session",
                "/edit-program", "/close-show", "/close-set", "/close-log", "/create-full-workout"
        );

        boolean isInsideFlow = safeZone.stream().anyMatch(uri::startsWith);

        // exit pages where session is normally cleared
        List<String> exitPoints = List.of("/home", "/my-workouts", "/profile", "/statistics", "/settings");

        boolean isExitPoint = exitPoints.stream().anyMatch(uri::startsWith);

        // --- New logic for /create ---
        if (uri.startsWith("/create")) {
            String newProgramParam = request.getParameter("newProgram");
            if ("true".equals(newProgramParam)) {
                // only clear session if starting a new program intentionally
                sessionModelService.clearProgramCreationSession(request.getSession());
            }
        }
        else if (isExitPoint) {
            // normal session cleanup for other exit pages
            sessionModelService.clearProgramCreationSession(request.getSession());
        }

        return true;
    }
}