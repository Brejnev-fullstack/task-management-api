
package com.example.taskmanager.controller;

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.LoginResponse;
import com.example.taskmanager.service.JwtService;
import com.example.taskmanager.service.PasswordResetService;
import com.example.taskmanager.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import com.example.taskmanager.dto.ForgotPasswordRequest;
import com.example.taskmanager.dto.ResetPasswordRequest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private TokenBlacklistService tokenBlacklistService;
    private PasswordResetService passwordResetService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        passwordResetService = mock(PasswordResetService.class);

        authController = new AuthController(
                authenticationManager,
                jwtService,
                tokenBlacklistService,
                passwordResetService
        );
    }

    //Tester l’authentification
    @Test
    void loginAvecIdentifiantsValidesRetourneLesTokens() {
        // Arrange : préparer les données et les comportements simulés
        LoginRequest request = new LoginRequest();
        request.setUsername("brejnev");
        request.setPassword("MotDePasse123!");

        when(jwtService.generateToken("brejnev"))
                .thenReturn("access-token-test");

        when(jwtService.generateRefreshToken("brejnev"))
                .thenReturn("refresh-token-test");

        // Act : appeler directement la méthode du contrôleur
        LoginResponse response = authController.login(request);

        // Assert : vérifier le résultat
        assertNotNull(response);
        assertEquals("Connexion réussie", response.getMessage());
        assertEquals("access-token-test", response.getToken());
        assertEquals("refresh-token-test", response.getRefreshToken());

        // Vérifier les interactions avec les dépendances
        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );

        verify(jwtService).generateToken("brejnev");
        verify(jwtService).generateRefreshToken("brejnev");
    }

    //Tester le refus d’authentification
    @Test
    void loginAvecIdentifiantsInvalidesNeGenerePasDeTokens() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("brejnev");
        request.setPassword("mauvais-mot-de-passe");

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(new BadCredentialsException("Identifiants invalides"));

        // Act + Assert
        assertThrows(
                BadCredentialsException.class,
                () -> authController.login(request)
        );

        // Vérifier que les tokens n'ont jamais été générés
        verify(jwtService, never()).generateToken("brejnev");
        verify(jwtService, never()).generateRefreshToken("brejnev");
    }

    //Tester la méthode logout()
    @Test
    void logoutAvecHeaderValideRevoqueLeToken() {
        // Arrange
        String token = "access-token-test";
        String authorizationHeader = "Bearer " + token;

        // Act
        String resultat = authController.logout(authorizationHeader);

        // Assert
        assertEquals("Déconnexion réussie", resultat);
        verify(tokenBlacklistService).revokeToken(token);
    }
    @Test
    void logoutSansHeaderLeveUneException() {
        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.logout(null)
        );

        verify(tokenBlacklistService, never()).revokeToken(any());
    }
    @Test
    void logoutAvecHeaderInvalideLeveUneException() {
        // Arrange
        String authorizationHeader = "Basic access-token-test";

        // Act + Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authController.logout(authorizationHeader)
        );

        verify(tokenBlacklistService, never()).revokeToken(any());
    }

    //Tester la méthode refresh()

    @Test
    void refreshAvecTokenValideGenereDeNouveauxTokensEtRevoqueAncienToken() {
        // Arrange
        String ancienRefreshToken = "ancien-refresh-token";

        when(tokenBlacklistService.isRevoked(ancienRefreshToken))
                .thenReturn(false);

        when(jwtService.isTokenValid(ancienRefreshToken))
                .thenReturn(true);

        when(jwtService.extractTokenType(ancienRefreshToken))
                .thenReturn("refresh");

        when(jwtService.extractUsername(ancienRefreshToken))
                .thenReturn("brejnev");

        when(jwtService.generateToken("brejnev"))
                .thenReturn("nouvel-access-token");

        when(jwtService.generateRefreshToken("brejnev"))
                .thenReturn("nouveau-refresh-token");

        // Act
        LoginResponse response = authController.refresh(ancienRefreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("Token renouvelé", response.getMessage());
        assertEquals("nouvel-access-token", response.getToken());
        assertEquals("nouveau-refresh-token", response.getRefreshToken());

        verify(tokenBlacklistService).isRevoked(ancienRefreshToken);
        verify(jwtService).isTokenValid(ancienRefreshToken);
        verify(tokenBlacklistService).revokeToken(ancienRefreshToken);
    }

    //Refuser un refresh token déjà révoqué

    @Test
    void refreshAvecTokenRevoqueLeveUneException() {
        // Arrange
        String refreshToken = "refresh-token-revoque";

        when(tokenBlacklistService.isRevoked(refreshToken))
                .thenReturn(true);

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authController.refresh(refreshToken)
        );

        assertEquals("Refresh token révoqué", exception.getMessage());

        // Le contrôleur doit s'arrêter immédiatement
        verify(jwtService, never()).isTokenValid(refreshToken);
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    //tester un refresh token invalide

    @Test
    void refreshAvecTokenInvalideLeveUneException() {
        // Arrange : préparer un token invalide
        String refreshToken = "refresh-token-invalide";

        when(tokenBlacklistService.isRevoked(refreshToken))
                .thenReturn(false);

        when(jwtService.isTokenValid(refreshToken))
                .thenReturn(false);

        // Act + Assert : vérifier l'exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authController.refresh(refreshToken)
        );

        assertEquals(
                "Refresh token invalide ou expiré",
                exception.getMessage()
        );

        // Vérifier qu'aucun nouveau token n'est généré
        verify(jwtService, never()).extractTokenType(refreshToken);
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());

        // Vérifier que l'ancien token n'est pas révoqué une seconde fois
        verify(tokenBlacklistService, never()).revokeToken(refreshToken);
    }

    //tester un mauvais type de token

    @Test
    void refreshAvecTokenDeTypeAccessLeveUneException() {
        // Arrange
        String token = "access-token-test";

        when(tokenBlacklistService.isRevoked(token))
                .thenReturn(false);

        when(jwtService.isTokenValid(token))
                .thenReturn(true);

        when(jwtService.extractTokenType(token))
                .thenReturn("access");

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authController.refresh(token)
        );

        assertEquals(
                "Refresh token invalide ou expiré",
                exception.getMessage()
        );

        // Le contrôleur ne doit pas continuer le renouvellement
        verify(jwtService, never()).extractUsername(token);
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
        verify(tokenBlacklistService, never()).revokeToken(token);
    }

    //Tester forgotPassword()

    @Test
    void forgotPasswordAvecEmailValideDemandeLaCreationDuToken() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("brejnev@example.com");

        // Act
        String resultat = authController.forgotPassword(request);

        // Assert
        assertEquals(
                "Si un compte existe avec cet email, un lien de réinitialisation sera envoyé.",
                resultat
        );

        verify(passwordResetService)
                .createResetToken("brejnev@example.com");
    }

    //Tester resetPassword()

    @Test
    void resetPasswordAvecDemandeValideReinitialiseLeMotDePasse() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token-reinitialisation-test");
        request.setNewPassword("NouveauMotDePasse123!");

        // Act
        String resultat = authController.resetPassword(request);

        // Assert
        assertEquals(
                "Mot de passe réinitialisé avec succès",
                resultat
        );

        verify(passwordResetService).resetPassword(
                "token-reinitialisation-test",
                "NouveauMotDePasse123!"
        );
    }

    //tester les erreurs des services

    @Test
    void forgotPasswordSiLeServiceEchouePropageException() {
        // Arrange
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("brejnev@example.com");

        doThrow(new RuntimeException("Erreur du service"))
                .when(passwordResetService)
                .createResetToken("brejnev@example.com");

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authController.forgotPassword(request)
        );

        assertEquals("Erreur du service", exception.getMessage());

        verify(passwordResetService)
                .createResetToken("brejnev@example.com");
    }

    //tester l'échec de resetPassword()

    @Test
    void resetPasswordSiLeServiceEchouePropageException() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("token-invalide");
        request.setNewPassword("NouveauMotDePasse123!");

        doThrow(new IllegalArgumentException("Token invalide ou expiré"))
                .when(passwordResetService)
                .resetPassword(
                        "token-invalide",
                        "NouveauMotDePasse123!"
                );

        // Act + Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authController.resetPassword(request)
        );

        assertEquals(
                "Token invalide ou expiré",
                exception.getMessage()
        );

        verify(passwordResetService).resetPassword(
                "token-invalide",
                "NouveauMotDePasse123!"
        );
    }

}
