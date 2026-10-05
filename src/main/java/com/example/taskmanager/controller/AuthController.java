package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ForgotPasswordRequest;
import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.LoginResponse;
import com.example.taskmanager.dto.ResetPasswordRequest;
import com.example.taskmanager.service.JwtService;
import com.example.taskmanager.service.PasswordResetService;
import com.example.taskmanager.service.TokenBlacklistService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService, TokenBlacklistService tokenBlacklistService,
                          PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.passwordResetService = passwordResetService;
    }




    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getUsername());
        String refreshToken = jwtService.generateRefreshToken(request.getUsername());

        return new LoginResponse(
                "Connexion réussie",
                token,
                refreshToken
                //request.getUsername()

        );
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestParam String refreshToken) {

        if (tokenBlacklistService.isRevoked(refreshToken)) {
            throw new IllegalArgumentException("Refresh token révoqué");
        }
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException(
                    "Refresh token invalide ou expiré"
            );
        }

        try {


        String tokenType = jwtService.extractTokenType(refreshToken);

        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Le token fourni n'est pas un refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);

        String newToken = jwtService.generateToken(username);
        String newRefreshToken = jwtService.generateRefreshToken(username);
        tokenBlacklistService.revokeToken(refreshToken);

        return new LoginResponse(
                "Token renouvelé",
                newToken,
                newRefreshToken
        );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Refresh token invalide ou expiré"
            );
        }
    }

    @PostMapping("/logout")
    public String logout(
            @RequestHeader(value = "Authorization", required = false)
            String authorizationHeader
    ) {
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException(
                    "Header Authorization invalide"
            );
        }

        String token = authorizationHeader.substring(7);
        tokenBlacklistService.revokeToken(token);
        return "Déconnexion réussie";
    }
    /*public String logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.substring(7);
        tokenBlacklistService.revokeToken(token);
        return "Déconnexion réussie";
    }*/


    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.createResetToken(request.getEmail());

        return "Si un compte existe avec cet email, un lien de réinitialisation sera envoyé.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return "Mot de passe réinitialisé avec succès";
    }

}