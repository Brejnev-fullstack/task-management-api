
package com.example.taskmanager.service;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.entity.UserRole;
import com.example.taskmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
        String username = "brejnev";

        User user = new User();
        user.setUsername(username);
        user.setPassword("mot_de_passe_hache");
        user.setRole(UserRole.USER);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        UserDetails result =
                customUserDetailsService.loadUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("mot_de_passe_hache", result.getPassword());

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserDoesNotExist() {

        String username = "utilisateur_inconnu";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals(
                "Utilisateur introuvable : " + username,
                exception.getMessage()
        );

        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_shouldAssignCorrectRole() {

        String username = "utilisateur_role";

        User user = new User();
        user.setUsername(username);
        user.setPassword("mot_de_passe_hache");
        user.setRole(UserRole.USER);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));


        UserDetails result =
                customUserDetailsService.loadUserByUsername(username);

        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")
                ));
    }
}