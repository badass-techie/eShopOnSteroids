package com.badasstechie.identity.dto;

public record UserRequest(
        String name,
        String email,
        String password,
        String bio,
        String image,
        String role) {
}
