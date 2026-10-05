package com.example.taskmanager.dto;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.entity.UserRole;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private UserRole role;

    public UserResponse(
            Long id,
            String username,
            String email,
            UserRole role
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}