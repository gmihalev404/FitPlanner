package com.example.fitplanner.util;

import org.springframework.stereotype.Component;

@Component
public class UnitValidator {
    public boolean isValidUsername(String input) {
        if (input == null || input.isBlank()) return false;
        return input.matches("^[A-Za-z0-9_]{4,64}$");
    }

    public boolean isValidEmail(String input) {
        if (input == null || input.isBlank()) return false;
        return input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}