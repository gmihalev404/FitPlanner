package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("SELECT p FROM Program p WHERE p.user.id = :userId")
    List<Program> getByUserId(@Param(value = "userId") Long userId);

    @Query("")
    Optional<Program> findFirstByOrderByCreatedAtDesc();
}