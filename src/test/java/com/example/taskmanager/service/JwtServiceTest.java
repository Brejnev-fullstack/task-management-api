package com.example.taskmanager.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "MaCleSecreteDeTestPourLesTokensJWT2026";
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void generateTokenShouldCreateValidAccessToken() {
        String token = jwtService.generateToken("brejnev");
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("brejnev", jwtService.extractUsername(token));
        assertEquals("access", jwtService.extractTokenType(token));
    }

    @Test
    void generateRefreshTokenShouldCreateValidRefreshToken() {
        String token = jwtService.generateRefreshToken("brejnev");
        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("brejnev", jwtService.extractUsername(token));
        assertEquals("refresh", jwtService.extractTokenType(token));
    }

    @Test
    void extractUsernameShouldReturnCorrectUsername() {
        String token = jwtService.generateToken("utilisateur123");
        String username = jwtService.extractUsername(token);
        assertEquals("utilisateur123", username);
    }

    @Test
    void extractTokenTypeShouldReturnAccessForAccessToken() {
        String token = jwtService.generateToken("brejnev");
        String type = jwtService.extractTokenType(token);
        assertEquals("access", type);
    }

    @Test
    void extractTokenTypeShouldReturnRefreshForRefreshToken() {
        String token = jwtService.generateRefreshToken("brejnev");
        String type = jwtService.extractTokenType(token);
        assertEquals("refresh", type);
    }


    @Test
    void extractExpirationShouldReturnFutureDateForAccessToken() {
        long beforeGeneration = System.currentTimeMillis();

        String token = jwtService.generateToken("brejnev");

        Date expiration = jwtService.extractExpiration(token);

        long afterGeneration = System.currentTimeMillis();

        long oneHourInMillis = 60 * 60 * 1000L;

        assertNotNull(expiration);

        assertTrue(
                expiration.getTime() > afterGeneration,
                "La date d'expiration doit être dans le futur"
        );

        assertTrue(
                expiration.getTime() >= beforeGeneration + oneHourInMillis - 1000,
                "Le jeton doit expirer environ une heure après sa création"
        );

        assertTrue(
                expiration.getTime() <= afterGeneration + oneHourInMillis,
                "La date d'expiration ne doit pas dépasser une heure après la génération"
        );
    }

    @Test
    void isTokenValidShouldReturnFalseForMalformedToken() {
        String token = "ceci-nest-pas-un-jwt";
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValidShouldReturnFalseForExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        String expiredToken = Jwts.builder()
                .subject("brejnev")
                .claim("type", "access")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(key)
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken));
    }

    @Test
    void isTokenValidShouldReturnFalseWhenTokenUsesAnotherSecret() {
        String token = jwtService.generateToken("brejnev");
        JwtService anotherJwtService = new JwtService(
                "UneAutreCleSecreteDeTestPourLesJWT2026"
        );
        assertFalse(anotherJwtService.isTokenValid(token));
    }

    @Test
    void constructorShouldRejectSecretThatIsTooShort() {
        assertThrows(
                WeakKeyException.class,
                () -> new JwtService("cle-trop-courte")
        );
    }
}