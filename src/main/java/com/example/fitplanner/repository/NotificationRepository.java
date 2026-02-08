package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.beans.JavaBean;
import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n " +
            "WHERE n.observer.id = :userId " +
            "AND n.checked = false " +
            "ORDER BY n.date DESC")
    List<Notification> findUnreadByUser(@Param("userId") Long userId);}
