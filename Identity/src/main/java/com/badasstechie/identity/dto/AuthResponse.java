package com.badasstechie.identity.dto;

import java.time.Instant;

public record AuthResponse(
        String email,
        String accessToken,
        Instant expiresAt,
        String refreshToken) {
}
