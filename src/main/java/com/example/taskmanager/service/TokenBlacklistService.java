package com.example.taskmanager.service;

import com.example.taskmanager.entity.RevokedToken;
import com.example.taskmanager.repository.RevokedTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;


@Service
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;
    private final Set<String> blacklistedTokens = new HashSet<>();


    public TokenBlacklistService(
            RevokedTokenRepository revokedTokenRepository,
            JwtService jwtService
    ) {
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
    }


    public void revokeToken(String token) {

        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(token);

        Date expiration = jwtService.extractExpiration(token);
        revokedToken.setExpiresAt(
                expiration.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        revokedTokenRepository.save(revokedToken);
    }

    public boolean isRevoked(String token) {
        return revokedTokenRepository
                .findByToken(token)
                .isPresent();
    }

    @Transactional
    @Scheduled(
            fixedRate = 3600000,
            initialDelay = 60000
    )
    public void cleanupExpiredTokens() {
        revokedTokenRepository.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );
    }

}