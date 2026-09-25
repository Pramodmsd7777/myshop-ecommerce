package com.shop.dto;

import com.shop.model.Role;
import jakarta.validation.constraints.*;

public class AuthDtos {
    public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password) {}

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {}

    public record AuthResponse(String token, String tokenType, Long id, String name, String email, Role role) {}
}
