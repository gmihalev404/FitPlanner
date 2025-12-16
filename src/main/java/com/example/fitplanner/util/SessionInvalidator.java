package com.example.fitplanner.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

//@Component
public class SessionInvalidator extends OncePerRequestFilter {

    private boolean invalidated = false;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!invalidated) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            invalidated = true;
        }

        filterChain.doFilter(request, response);
    }
}