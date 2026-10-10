
package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.taskmanager.dto.TaskRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskControllerTest {

    private TaskService taskService;
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        taskController = new TaskController(taskService);
    }

    //getAllTasks()
    @Test
    void getAllTasksRetourneLaListeDesTaches() {
        // Arrange
        Task task = new Task();

        when(taskService.getAllTasks())
                .thenReturn(List.of(task));

        // Act
        List<TaskResponse> result = taskController.getAllTasks();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        // Verify
        verify(taskService).getAllTasks();
    }

    //Tester createTask()
    @Test
    void createTaskCreeUneTacheEtRetourneCreated() {
        // Arrange
        TaskRequest request = new TaskRequest();
        Task savedTask = new Task();

        when(taskService.createTask(request))
                .thenReturn(savedTask);

        // Act
        ResponseEntity<TaskResponse> response =
                taskController.createTask(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify
        verify(taskService).createTask(request);
    }

    //tester getTaskById()
    @Test
    void getTaskByIdRetourneLaTacheDemandee() {
        // Arrange
        Long id = 1L;
        Task task = new Task();

        when(taskService.getTaskById(id))
                .thenReturn(task);

        // Act
        TaskResponse response = taskController.getTaskById(id);

        // Assert
        assertNotNull(response);

        // Verify
        verify(taskService).getTaskById(id);
    }

    //Tester la modification d'une tâche
    @Test
    void updateTaskRetourneLaTacheModifiee() {
        // Arrange
        Long id = 1L;
        TaskRequest request = new TaskRequest();
        Task updatedTask = new Task();

        when(taskService.updateTask(id, request))
                .thenReturn(updatedTask);

        // Act
        ResponseEntity<TaskResponse> response =
                taskController.updateTaskBysId(id, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Verify
        verify(taskService).updateTask(id, request);
    }

    //Tester la suppression d'une tâche
    @Test
    void deleteTaskSupprimeLaTacheEtRetourneNoContent() {
        // Arrange
        Long id = 1L;

        // Act
        ResponseEntity<Void> response =
                taskController.deleteTask(id);

        // Assert
        assertNotNull(response);
        assertEquals(
                HttpStatus.NO_CONTENT,
                response.getStatusCode()
        );
        assertNull(response.getBody());

        // Verify
        verify(taskService).deleteTask(id);
    }

    //Tester les cas limites
    @Test
    void getAllTasksRetourneUneListeVideQuandAucuneTacheExiste() {
        // Arrange
        when(taskService.getAllTasks())
                .thenReturn(List.of());

        // Act
        List<TaskResponse> result = taskController.getAllTasks();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify
        verify(taskService).getAllTasks();
    }

    //Tester la propagation d'une erreur du service
    @Test
    void getTaskByIdPropageUneExceptionQuandLeServiceEchoue() {
        // Arrange
        Long id = 1L;

        when(taskService.getTaskById(id))
                .thenThrow(new RuntimeException("Erreur interne"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskController.getTaskById(id)
        );

        assertEquals("Erreur interne", exception.getMessage());

        // Verify
        verify(taskService).getTaskById(id);
    }

    //Tester une erreur lors de la création
    @Test
    void createTaskPropageUneExceptionQuandLeServiceEchoue() {
        // Arrange
        TaskRequest request = new TaskRequest();

        when(taskService.createTask(request))
                .thenThrow(new RuntimeException("Impossible de créer la tâche"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskController.createTask(request)
        );

        assertEquals(
                "Impossible de créer la tâche",
                exception.getMessage()
        );

        // Verify
        verify(taskService).createTask(request);
    }

    //Tester l'échec de updateTaskBysId()
    @Test
    void updateTaskPropageUneExceptionQuandLeServiceEchoue() {
        // Arrange
        Long id = 1L;
        TaskRequest request = new TaskRequest();

        when(taskService.updateTask(id, request))
                .thenThrow(new RuntimeException("Impossible de modifier la tâche"));

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskController.updateTaskBysId(id, request)
        );

        assertEquals(
                "Impossible de modifier la tâche",
                exception.getMessage()
        );

        // Verify
        verify(taskService).updateTask(id, request);
    }

    //Tester l'échec de la suppression
    @Test
    void deleteTaskPropageUneExceptionQuandLeServiceEchoue() {
        // Arrange
        Long id = 1L;

        org.mockito.Mockito.doThrow(
                new RuntimeException("Impossible de supprimer la tâche")
        ).when(taskService).deleteTask(id);

        // Act + Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskController.deleteTask(id)
        );

        assertEquals(
                "Impossible de supprimer la tâche",
                exception.getMessage()
        );

        // Verify
        verify(taskService).deleteTask(id);
    }

}
