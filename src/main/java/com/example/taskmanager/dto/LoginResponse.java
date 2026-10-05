package com.example.taskmanager.dto;

public class LoginResponse {

    private String message;
    private String token;
    private String refreshToken;

    public LoginResponse(String message, String token, String refreshToken) {
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }
}