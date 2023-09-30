package com.badasstechie.identity.dto;

public record RefreshTokenRequest(
        String email,
        String refreshToken) {
}
