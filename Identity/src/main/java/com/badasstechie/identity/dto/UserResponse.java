package com.badasstechie.identity.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        String bio,
        String image,
        String role,
        Instant created,
        boolean active
) {
}
