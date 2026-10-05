package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> getAllTasks() {
        return taskService.getAllTasks()
                .stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }
   /* public List<Task> getAllTasks(){
        return taskService.getAllTasks();
    }*/

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        Task savedTask = taskService.createTask(request);
        TaskResponse response = TaskResponse.fromEntity(savedTask);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    /*public Task createTask(@Valid @RequestBody TaskRequest request){
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return taskService.createTask(task);
    }
    /*public Task createTask(@RequestBody Task task){
        return taskService.createTask(task);
    }*/

    @GetMapping("/tasks/{id}")
    public TaskResponse getTaskById(@PathVariable Long id){
        Task task = taskService.getTaskById(id);
        return TaskResponse.fromEntity(task);

    }
   /* public Task getTaskById(@PathVariable Long id){
        return taskService.getTaskById(id);
    }*/

    @PutMapping("/tasks/{id}")
    public  ResponseEntity<TaskResponse> updateTaskBysId(@PathVariable Long id, @Valid @RequestBody TaskRequest request){
        Task updatedTask = taskService.updateTask(id, request);
        TaskResponse response = TaskResponse.fromEntity(updatedTask);
        return ResponseEntity.ok(response);
        //return taskService.updateTask(id,task);
    }

    @DeleteMapping("/tasks/{id}")
        public ResponseEntity<Void> deleteTask(@PathVariable Long id){
         taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

}
