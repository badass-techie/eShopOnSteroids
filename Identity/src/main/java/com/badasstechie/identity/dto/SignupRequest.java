package com.badasstechie.identity.dto;

public record SignupRequest(
        String username,
        String email,
        String password,
        String bio,
        String image,
        String role
        ) {
}
