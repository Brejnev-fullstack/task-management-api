package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserRequest;
import com.example.taskmanager.dto.UserResponse;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request
            ){

        User savedUser = userService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.fromEntity(savedUser));

    }

    @GetMapping("/admin-test")
    public String adminTest() {
        return "Accès administrateur autorisé";
    }


}