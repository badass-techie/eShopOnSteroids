package com.badasstechie.identity.service;

import com.badasstechie.identity.model.RefreshToken;
import com.badasstechie.identity.model.User;
import com.badasstechie.identity.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .owner(user)
                .created(Instant.now())
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public void validateRefreshToken(String email, String token) {
        refreshTokenRepository.findByOwnerEmailAndToken(email, token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
    }

    @Transactional
    public void deleteRefreshTokensByUser(String email) {
        refreshTokenRepository.deleteAllByOwnerEmail(email);
    }
}