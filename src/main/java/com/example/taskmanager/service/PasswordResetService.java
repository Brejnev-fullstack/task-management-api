package com.example.taskmanager.service;

import com.example.taskmanager.entity.PasswordResetToken;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.PasswordResetTokenRepository;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createResetToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return;
        }

        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(
                LocalDateTime.now().plusMinutes(15)
        );

        tokenRepository.save(resetToken);
    }
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() ->
                        new IllegalArgumentException("Token invalide")
                );

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token déjà utilisé");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    @Transactional
    @Scheduled(
            fixedRate = 3600000,
            initialDelay = 60000
    )
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(
                LocalDateTime.now()
        );
    }
}