
package com.example.taskmanager.service;

import com.example.taskmanager.entity.PasswordResetToken;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.PasswordResetTokenRepository;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void createResetToken_shouldSaveTokenWhenUserExists() {
        String email = "user@example.com";
        User user = new User();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));
        LocalDateTime before = LocalDateTime.now();
        passwordResetService.createResetToken(email);
        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(tokenRepository).save(captor.capture());
        PasswordResetToken savedToken = captor.getValue();
        assertNotNull(savedToken.getToken());
        assertTrue(savedToken.getToken().matches("[A-Za-z0-9_-]{43}"));
        assertSame(user, savedToken.getUser());

        assertFalse(savedToken.getExpiresAt().isBefore(
                before.plusMinutes(15)
        ));
        assertFalse(savedToken.getExpiresAt().isAfter(
                after.plusMinutes(15)
        ));
    }

    @Test
    void createResetToken_shouldNotSaveTokenWhenUserDoesNotExist() {
        String email = "unknown@example.com";
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        passwordResetService.createResetToken(email);
        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndMarkTokenAsUsed() {
        String token = "token-valide";
        String newPassword = "NouveauMotDePasse123!";
        String encodedPassword = "mot-de-passe-encode";

        User user = new User();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token))
                .thenReturn(Optional.of(resetToken));

        when(passwordEncoder.encode(newPassword))
                .thenReturn(encodedPassword);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.resetPassword(token, newPassword);

        assertEquals(encodedPassword, user.getPassword());
        assertTrue(resetToken.isUsed());

        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
    }

    @Test
    void resetPassword_shouldThrowExceptionWhenTokenDoesNotExist() {
        String token = "token-inexistant";

        when(tokenRepository.findByToken(token))
                .thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(
                        token,
                        "NouveauMotDePasse123!"
                )
        );

        assertEquals("Token invalide", exception.getMessage());

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_shouldThrowExceptionWhenTokenIsAlreadyUsed() {
        String token = "token-deja-utilise";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        resetToken.setUsed(true);

        when(tokenRepository.findByToken(token))
                .thenReturn(Optional.of(resetToken));
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(
                        token,
                        "NouveauMotDePasse123!"
                )
        );

        assertEquals("Token déjà utilisé", exception.getMessage());

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_shouldThrowExceptionWhenTokenIsExpired() {
        String token = "token-expire";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().minusMinutes(5));
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token))
                .thenReturn(Optional.of(resetToken));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(
                        token,
                        "NouveauMotDePasse123!"
                )
        );

        // Assert
        assertEquals("Token expiré", exception.getMessage());

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never())
                .save(any(PasswordResetToken.class));
    }

    @Test
    void cleanupExpiredTokens_shouldDeleteTokensExpiredBeforeNow() {
        LocalDateTime before = LocalDateTime.now();
        passwordResetService.cleanupExpiredTokens();
        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<LocalDateTime> captor =
                ArgumentCaptor.forClass(LocalDateTime.class);

        verify(tokenRepository)
                .deleteByExpiresAtBefore(captor.capture());

        LocalDateTime cutoff = captor.getValue();

        assertFalse(cutoff.isBefore(before));
        assertFalse(cutoff.isAfter(after));
    }
}