package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks(){
        return taskRepository.findAll();
    }

    public Task createTask(TaskRequest request){
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        return taskRepository.save(task);
        /*return taskRepository.save(task);*/
    }

    public Task getTaskById(Long id){
        return taskRepository.findById(id).orElseThrow(() ->
                new TaskNotFoundException("La tâche avec l'id " + id + " n'existe pas"));
        //return taskRepository.findById(id).orElse(null);
    }

    public Task updateTask(Long id, TaskRequest requestUpdate){
        //Task existingTask = taskRepository.findById(id).orElse(null);
        Task existingTask = taskRepository.findById(id).orElseThrow(()->
                new TaskNotFoundException("La tâche avec l'id " + id + " n'existe pas"));

        /*if(existingTask == null){
            return null;
        }*/

        existingTask.setTitle(requestUpdate.getTitle());
        existingTask.setDescription(requestUpdate.getDescription());
        existingTask.setStatus(requestUpdate.getStatus());
        existingTask.setPriority(requestUpdate.getPriority());
        existingTask.setDueDate(requestUpdate.getDueDate());

        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id){
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException(
                                "La tâche avec l'id " + id + " n'existe pas"
                        )
                );
        taskRepository.delete(existingTask);
        //taskRepository.deleteById(id);
    }

}
