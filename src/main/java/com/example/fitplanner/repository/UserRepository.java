package com.example.fitplanner.repository;

import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username LIKE :username")
    Optional<User> getByUsername(@Param(value = "username") String username);

    @Query("SELECT u FROM User u WHERE u.email LIKE :email")
    Optional<User> getByEmail(@Param(value = "email") String email);

    @Query("SELECT u FROM User u WHERE u.username LIKE :username AND u.password LIKE :password")
    Optional<User> getByUsernameAndPassword(@Param(value = "username") String username,
                                 @Param(value = "password") String password);

    @Query("SELECT u FROM User u WHERE u.email LIKE :email AND u.password LIKE :password")
    Optional<User> getByEmailAndPassword(@Param(value = "email") String email,
                                            @Param(value = "password") String password);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role = :role")
    Boolean existsByRole(@Param(value = "role") Role role);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.role = 'TRAINER' AND " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchTrainers(@Param("query") String query);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT u.streak FROM User u WHERE u.id = :userId")
    Integer findStreakById(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);
}