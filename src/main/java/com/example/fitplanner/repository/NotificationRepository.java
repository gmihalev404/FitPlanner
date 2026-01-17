package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.beans.JavaBean;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
