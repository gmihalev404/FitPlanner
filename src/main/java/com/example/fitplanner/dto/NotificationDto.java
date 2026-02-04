package com.example.fitplanner.dto;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationDto implements Serializable {
    private String name;

    private String description;

    private Long senderId;

    private Boolean checked = false;

    private LocalDateTime date;

    private String url;
}