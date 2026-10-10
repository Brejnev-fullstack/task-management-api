package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.entity.TaskPriority;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.exception.TaskNotFoundException;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessException;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskService taskService;

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        // Arrange
        Task task1 = new Task();
        Task task2 = new Task();
        List<Task> expectedTasks = List.of(task1, task2);
        when(taskRepository.findAll()).thenReturn(expectedTasks);

        List<Task> actualTasks = taskService.getAllTasks();
        assertNotNull(actualTasks);
        assertEquals(2, actualTasks.size());
        assertEquals(expectedTasks, actualTasks);

        verify(taskRepository).findAll();
    }

    @Test
    void getAllTasks_shouldReturnEmptyListWhenNoTasksExist() {

        when(taskRepository.findAll()).thenReturn(List.of());

        List<Task> actualTasks = taskService.getAllTasks();
        assertNotNull(actualTasks);
        assertEquals(0, actualTasks.size());
        verify(taskRepository).findAll();
    }

    @Test
    void createTask_shouldCopyRequestFieldsAndSaveTask() {
        // Arrange : préparer les données d'entrée
        TaskRequest request = new TaskRequest();
        request.setTitle("Apprendre les tests unitaires");
        request.setDescription("Maîtriser JUnit et Mockito");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.of(2026, 10, 20));

        // Créer un captor capable de récupérer un objet Task
        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(Task.class);

        // Simuler le retour du repository lors de la sauvegarde
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act : exécuter la vraie méthode du service
        Task actualTask = taskService.createTask(request);

        // Capturer la tâche réellement transmise à save()
        verify(taskRepository).save(taskCaptor.capture());

        // Récupérer l'objet capturé
        Task savedTask = taskCaptor.getValue();

        // Assert : vérifier la tâche retournée
        assertNotNull(actualTask);

        // Vérifier les champs de la tâche envoyée au repository
        assertEquals("Apprendre les tests unitaires", savedTask.getTitle());
        assertEquals("Maîtriser JUnit et Mockito", savedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, savedTask.getStatus());
        assertEquals(TaskPriority.HIGH, savedTask.getPriority());
        assertEquals(LocalDate.of(2026, 10, 20), savedTask.getDueDate());

        // Vérifier également le résultat retourné
        assertEquals(savedTask, actualTask);
    }

    @Test
    void createTask_shouldPropagateRepositoryException() {
        // Arrange : préparer une requête valide
        TaskRequest request = new TaskRequest();
        request.setTitle("Tester une erreur");
        request.setDescription("Vérifier la propagation");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.MEDIUM);
        request.setDueDate(LocalDate.of(2026, 10, 20));

        // Simuler une erreur du repository
        when(taskRepository.save(any(Task.class)))
                .thenThrow(new DataAccessException("Erreur de persistance") {});

        // Act & Assert : vérifier que l'exception remonte
        DataAccessException exception = assertThrows(
                DataAccessException.class,
                () -> taskService.createTask(request)
        );

        assertEquals("Erreur de persistance", exception.getMessage());

        // Vérifier que la sauvegarde a été tentée une seule fois
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskById_shouldReturnTaskWhenTaskExists() {
        // Arrange
        Long taskId = 1L;
        Task expectedTask = new Task();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(expectedTask));

        // Act
        Task actualTask = taskService.getTaskById(taskId);

        // Assert
        assertNotNull(actualTask);
        assertEquals(expectedTask, actualTask);

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_shouldThrowExceptionWhenTaskDoesNotExist() {
        // Arrange
        Long taskId = 99L;

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(taskId)
        );

        assertEquals(
                "La tâche avec l'id 99 n'existe pas",
                exception.getMessage()
        );

        verify(taskRepository).findById(taskId);
    }

    @Test
    void updateTask_shouldUpdateExistingTaskAndSaveIt() {
        // Arrange
        Long taskId = 1L;

        Task existingTask = new Task();
        existingTask.setTitle("Ancien titre");
        existingTask.setDescription("Ancienne description");
        existingTask.setStatus(TaskStatus.TODO);
        existingTask.setPriority(TaskPriority.LOW);
        existingTask.setDueDate(LocalDate.of(2026, 10, 15));

        TaskRequest requestUpdate = new TaskRequest();
        requestUpdate.setTitle("Nouveau titre");
        requestUpdate.setDescription("Nouvelle description");
        requestUpdate.setStatus(TaskStatus.IN_PROGRESS);
        requestUpdate.setPriority(TaskPriority.HIGH);
        requestUpdate.setDueDate(LocalDate.of(2026, 11, 20));

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask))
                .thenReturn(existingTask);

        // Act
        Task actualTask = taskService.updateTask(taskId, requestUpdate);

        // Assert
        assertNotNull(actualTask);
        assertEquals("Nouveau titre", actualTask.getTitle());
        assertEquals("Nouvelle description", actualTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, actualTask.getStatus());
        assertEquals(TaskPriority.HIGH, actualTask.getPriority());
        assertEquals(LocalDate.of(2026, 11, 20), actualTask.getDueDate());

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(existingTask);
    }

    @Test
    void updateTask_shouldThrowExceptionWhenTaskDoesNotExist() {
        // Arrange
        Long taskId = 99L;

        TaskRequest requestUpdate = new TaskRequest();
        requestUpdate.setTitle("Nouveau titre");
        requestUpdate.setDescription("Nouvelle description");
        requestUpdate.setStatus(TaskStatus.IN_PROGRESS);
        requestUpdate.setPriority(TaskPriority.HIGH);
        requestUpdate.setDueDate(LocalDate.of(2026, 11, 20));

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTask(taskId, requestUpdate)
        );

        assertEquals(
                "La tâche avec l'id 99 n'existe pas",
                exception.getMessage()
        );

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }


    @Test
    void deleteTask_shouldDeleteTaskWhenItExists() {
        // Arrange
        Long taskId = 1L;
        Task existingTask = new Task();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(existingTask));

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(existingTask);
    }


    @Test
    void deleteTask_shouldThrowExceptionWhenTaskDoesNotExist() {
        // Arrange
        Long taskId = 99L;

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        // Act & Assert
        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.deleteTask(taskId)
        );

        assertEquals(
                "La tâche avec l'id 99 n'existe pas",
                exception.getMessage()
        );

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
