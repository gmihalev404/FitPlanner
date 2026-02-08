package com.example.fitplanner.dto;

public record DashboardStatsDto(
        int currentStreak,
        double monthlyVolume,
        int successRate
) {
    public String getFormattedVolume() {
        return String.format("%.1f", monthlyVolume);
    }
}
