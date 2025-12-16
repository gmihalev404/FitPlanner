package com.example.fitplanner.repository;

import com.example.fitplanner.entity.enums.Role;
import com.example.fitplanner.entity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}