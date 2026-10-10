
package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void createUserCreeUnUtilisateurEtRetourneCreated() {
        // Arrange
        UserRequest request = new UserRequest();
        User savedUser = new User();

        when(userService.createUser(request))
                .thenReturn(savedUser);

        // Act
        ResponseEntity<UserResponse> response =
                userController.createUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());

        // Verify
        verify(userService).createUser(request);
    }

    @Test
    void adminTestRetourneLeMessageAttendu() {
        // Act
        String response = userController.adminTest();

        // Assert
        assertEquals(
                "Accès administrateur autorisé",
                response
        );
    }

    //Tester l'échec de création
    @Test
    void createUserPropageUneExceptionQuandLeServiceEchoue() {
        // Arrange
        UserRequest request = new UserRequest();

        when(userService.createUser(request))
                .thenThrow(new RuntimeException(
                        "Impossible de créer l'utilisateur"
                ));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userController.createUser(request)
        );

        assertEquals(
                "Impossible de créer l'utilisateur",
                exception.getMessage()
        );

        // Verify
        verify(userService).createUser(request);
    }

}
