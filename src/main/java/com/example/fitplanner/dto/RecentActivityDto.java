package com.example.fitplanner.dto;

import java.time.LocalDateTime;

import java.time.LocalDateTime;

public record RecentActivityDto(
        String title,
        String timeAgo,
        String typeColor,
        LocalDateTime sortDate // Critical for sorting mixed lists
) {}