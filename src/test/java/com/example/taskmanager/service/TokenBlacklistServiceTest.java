
package com.example.taskmanager.service;

import com.example.taskmanager.entity.RevokedToken;
import com.example.taskmanager.repository.RevokedTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private JwtService jwtService;

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(
                revokedTokenRepository,
                jwtService
        );
    }

    @Test
    void revokeTokenShouldSaveRevokedTokenWithExpiration() {
        String token = "jwt-token-test";
        Date expiration = new Date(
                System.currentTimeMillis() + 60 * 60 * 1000L
        );

        when(jwtService.extractExpiration(token)).thenReturn(expiration);

        tokenBlacklistService.revokeToken(token);

        ArgumentCaptor<RevokedToken> captor =
                ArgumentCaptor.forClass(RevokedToken.class);

        verify(revokedTokenRepository).save(captor.capture());
        RevokedToken savedToken = captor.getValue();
        assertEquals(token, savedToken.getToken());
        LocalDateTime expectedExpiration = expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        assertEquals(expectedExpiration, savedToken.getExpiresAt());
        verify(jwtService).extractExpiration(token);
    }

    @Test
    void isRevokedShouldReturnTrueWhenTokenExists() {
        String token = "jwt-token-revoque";
        RevokedToken revokedToken = new RevokedToken();
        revokedToken.setToken(token);
        when(revokedTokenRepository.findByToken(token))
                .thenReturn(Optional.of(revokedToken));

        boolean result = tokenBlacklistService.isRevoked(token);
        assertTrue(result);
        verify(revokedTokenRepository).findByToken(token);
    }

    @Test
    void isRevokedShouldReturnFalseWhenTokenDoesNotExist() {
        String token = "jwt-token-non-revoque";
        when(revokedTokenRepository.findByToken(token))
                .thenReturn(Optional.empty());
        boolean result = tokenBlacklistService.isRevoked(token);
        assertFalse(result);
        verify(revokedTokenRepository).findByToken(token);
    }

    @Test
    void cleanupExpiredTokensShouldAskRepositoryToDeleteExpiredTokens() {
        LocalDateTime beforeCall = LocalDateTime.now();
        tokenBlacklistService.cleanupExpiredTokens();
        LocalDateTime afterCall = LocalDateTime.now();
        ArgumentCaptor<LocalDateTime> captor =
                ArgumentCaptor.forClass(LocalDateTime.class);
        verify(revokedTokenRepository)
                .deleteByExpiresAtBefore(captor.capture());
        LocalDateTime cutoff = captor.getValue();

        assertNotNull(cutoff);
        assertFalse(cutoff.isBefore(beforeCall));
        assertFalse(cutoff.isAfter(afterCall));
    }
}