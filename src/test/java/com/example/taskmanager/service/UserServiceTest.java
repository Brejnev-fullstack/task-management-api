
package com.example.taskmanager.service;

import org.mockito.ArgumentCaptor;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.exception.UserAlreadyExistsException;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.taskmanager.dto.UserRequest;
import com.example.taskmanager.entity.UserRole;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void existsByUsername_shouldReturnTrueWhenUserExists() {
        // Arrange
        String username = "brejnev";
        User user = new User();
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));

        // Act
        boolean result = userService.existsByUsername(username);

        // Assert
        assertTrue(result);

        verify(userRepository).findByUsername(username);
    }
    @Test
    void existsByUsername_shouldReturnFalseWhenUserDoesNotExist() {
        // Arrange
        String username = "utilisateur_inconnu";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        // Act
        boolean result = userService.existsByUsername(username);

        // Assert
        assertFalse(result);

        verify(userRepository).findByUsername(username);
    }
    @Test
    void existsByEmail_shouldReturnTrueWhenEmailExists() {
        // Arrange
        String email = "user@example.com";
        User user = new User();
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        // Act
        boolean result = userService.existsByEmail(email);

        // Assert
        assertTrue(result);

        verify(userRepository).findByEmail(email);
    }
    @Test
    void existsByEmail_shouldReturnFalseWhenEmailDoesNotExist() {
        // Arrange
        String email = "inconnu@example.com";
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // Act
        boolean result = userService.existsByEmail(email);

        // Assert
        assertFalse(result);

        verify(userRepository).findByEmail(email);
    }

    /*@Test
    void createUser_shouldCreateUserWhenDataIsValid() {
        // Arrange : préparer la requête
        UserRequest request = new UserRequest();
        request.setUsername("nouvel_utilisateur");
        request.setEmail("nouveau@example.com");
        request.setPassword("MotDePasse123!");

        // Simuler l'absence de doublons
        when(userRepository.findByUsername("nouvel_utilisateur"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("nouveau@example.com"))
                .thenReturn(Optional.empty());

        // Simuler l'encodage du mot de passe
        when(passwordEncoder.encode("MotDePasse123!"))
                .thenReturn("mot_de_passe_hache");

        // Simuler la sauvegarde de l'utilisateur
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act : exécuter la vraie méthode
        User actualUser = userService.createUser(request);

        // Assert : vérifier le résultat
        assertNotNull(actualUser);
        assertEquals("nouvel_utilisateur", actualUser.getUsername());
        assertEquals("nouveau@example.com", actualUser.getEmail());
        assertEquals("mot_de_passe_hache", actualUser.getPassword());
        assertEquals(UserRole.USER, actualUser.getRole());

        // Vérifier les interactions essentielles
        verify(passwordEncoder).encode("MotDePasse123!");
        verify(userRepository, times(1)).save(any(User.class));
    }
*/

    @Test
    void createUser_shouldCreateUserWhenDataIsValid() {
        // Arrange : préparer la requête
        UserRequest request = new UserRequest();
        request.setUsername("nouvel_utilisateur");
        request.setEmail("nouveau@example.com");
        request.setPassword("MotDePasse123!");

        // Simuler l'absence de doublons
        when(userRepository.findByUsername("nouvel_utilisateur"))
                .thenReturn(Optional.empty());

        when(userRepository.findByEmail("nouveau@example.com"))
                .thenReturn(Optional.empty());

        // Simuler l'encodage du mot de passe
        when(passwordEncoder.encode("MotDePasse123!"))
                .thenReturn("mot_de_passe_hache");

        // Simuler la sauvegarde et retourner l'utilisateur reçu
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act : exécuter le vrai service
        User actualUser = userService.createUser(request);

        // Assert : vérifier le résultat retourné
        assertNotNull(actualUser);
        assertEquals("nouvel_utilisateur", actualUser.getUsername());
        assertEquals("nouveau@example.com", actualUser.getEmail());
        assertEquals("mot_de_passe_hache", actualUser.getPassword());
        assertEquals(UserRole.USER, actualUser.getRole());

        // Capturer l'utilisateur transmis à save()
        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        // Récupérer l'objet transmis au repository
        User savedUser = userCaptor.getValue();

        // Vérifier les données réellement envoyées à la sauvegarde
        assertEquals("nouvel_utilisateur", savedUser.getUsername());
        assertEquals("nouveau@example.com", savedUser.getEmail());
        assertEquals("mot_de_passe_hache", savedUser.getPassword());
        assertEquals(UserRole.USER, savedUser.getRole());

        // Vérifier l'encodage du mot de passe
        verify(passwordEncoder).encode("MotDePasse123!");
    }
    @Test
    void createUser_shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Arrange : préparer la requête
        UserRequest request = new UserRequest();
        request.setUsername("utilisateur_existant");
        request.setEmail("nouveau@example.com");
        request.setPassword("MotDePasse123!");
        User existingUser = new User();

        when(userRepository.findByUsername("utilisateur_existant"))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert : vérifier l'exception
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        assertEquals(
                "Le nom d'utilisateur existe déjà",
                exception.getMessage()
        );

        // Vérifier qu'aucun utilisateur n'est sauvegardé
        verify(userRepository, never()).save(any(User.class));

        // Vérifier que le mot de passe n'est pas encodé
        verify(passwordEncoder, never()).encode(any(String.class));
    }

    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange : préparer la requête
        UserRequest request = new UserRequest();
        request.setUsername("nouvel_utilisateur");
        request.setEmail("existant@example.com");
        request.setPassword("MotDePasse123!");

        // Le nom d'utilisateur est disponible
        when(userRepository.findByUsername("nouvel_utilisateur"))
                .thenReturn(Optional.empty());

        // L'adresse e-mail appartient déjà à un utilisateur
        User existingUser = new User();

        when(userRepository.findByEmail("existant@example.com"))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert : vérifier l'exception
        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        assertEquals(
                "L'adresse email existe déjà",
                exception.getMessage()
        );

        // Aucun utilisateur ne doit être sauvegardé
        verify(userRepository, never()).save(any(User.class));

        // Le mot de passe ne doit pas être encodé
        verify(passwordEncoder, never()).encode(any(String.class));
    }
}