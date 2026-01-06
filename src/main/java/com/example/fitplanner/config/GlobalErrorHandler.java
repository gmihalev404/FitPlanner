package com.example.fitplanner.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalErrorHandler {

    // 1. Handle Invalid Parameters (like /api/user/abc instead of /api/user/1)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid Parameter: The value '" + ex.getValue() + "' is not valid for " + ex.getName());
    }

    // 2. Handle Page Not Found (The Invalid URL case)
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex) {
        // This returns your HTML file (e.g., src/main/resources/templates/notfound.html)
        return "page-not-found";
    }

    // 3. The "Catch-All" Safety Net (For actual server crashes/bugs)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralError(Exception ex) {
        // Log the error so you can see it in the console
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred. Please check your request.");
    }
}
