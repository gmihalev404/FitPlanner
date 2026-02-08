package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Milestone;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    @Query("SELECT m FROM Milestone m WHERE m.user.id = :userId ORDER BY m.achievedAt DESC")
    List<Milestone> findRecentMilestones(@Param("userId") Long userId, Pageable pageable);
}
