package com.example.taskmanager.repository;

import com.example.taskmanager.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    void deleteByExpiresAtBefore(LocalDateTime date);
}