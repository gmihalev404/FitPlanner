package com.example.fitplanner.repository;

import com.example.fitplanner.entity.model.ProgramRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProgramRatingRepository extends JpaRepository<ProgramRating, Long> {

    boolean existsByUserIdAndProgramId(Long userId, Long programId);

    Optional<ProgramRating> findByUserIdAndProgramId(Long userId, Long programId);

    @Query("""
       SELECT AVG(pr.rating)
       FROM ProgramRating pr
       WHERE pr.program.id = :programId
       """)
    Double findAverageRating(@Param("programId") Long programId);

    long countByProgramId(Long programId);
}
