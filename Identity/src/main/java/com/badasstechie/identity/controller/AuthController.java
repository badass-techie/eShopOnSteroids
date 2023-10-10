package com.badasstechie.identity.controller;

import com.badasstechie.identity.dto.AuthRequest;
import com.badasstechie.identity.dto.AuthResponse;
import com.badasstechie.identity.dto.RefreshTokenRequest;
import com.badasstechie.identity.service.AuthService;
import com.badasstechie.identity.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/auth_token")
    @Operation(summary = "Request an access token")
    public AuthResponse requestAccessToken(@RequestBody AuthRequest authRequest) {
        return authService.requestAccessToken(authRequest);
    }

    @PostMapping("/refresh_token")
    @Operation(summary = "Refresh an access token")
    public AuthResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authService.refreshAccessToken(refreshTokenRequest);
    }

    @GetMapping("/logout/{email}")
    @Operation(summary = "Clear all your refresh tokens")
    public ResponseEntity<String> logout(@PathVariable String email) {
        refreshTokenService.deleteRefreshTokensByUser(email);
        return new ResponseEntity<>("You will be signed out shortly", HttpStatus.ACCEPTED);
    }
}
