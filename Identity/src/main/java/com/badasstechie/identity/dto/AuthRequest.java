package com.badasstechie.identity.dto;

public record AuthRequest(
        String email,
        String password) {
}
