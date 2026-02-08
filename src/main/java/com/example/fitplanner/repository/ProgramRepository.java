package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    @Query("SELECT p FROM Program p WHERE p.user.id = :userId")
    List<Program> getByUserId(@Param(value = "userId") Long userId);

    @Query("SELECT p FROM Program p " +
            "WHERE p.isPublic = true " +
            "AND p.user.id != :currentUserId " +
            "AND p.user.role = com.example.fitplanner.entity.enums.Role.TRAINER " +
            "ORDER BY p.rating DESC")
    List<Program> findTopRatedTrainerPrograms(@Param("currentUserId") Long currentUserId, Pageable pageable);

    @Query("SELECT p FROM Program p WHERE p.isPublic = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Program> searchPublicPrograms(@Param("query") String query);

    @Query("SELECT p FROM Program p WHERE p.isPublic = true")
    List<Program> findAllByIsPublicTrue();

    @Query("SELECT p FROM Program p WHERE p.user.id = :userId")
    List<Program> findAllByUserId(Long userId);
}