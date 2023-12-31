package com.badasstechie.identity.service;

import com.badasstechie.identity.dto.AuthRequest;
import com.badasstechie.identity.dto.UserRequest;
import com.badasstechie.identity.dto.UserResponse;
import com.badasstechie.identity.model.User;
import com.badasstechie.identity.model.UserRole;
import com.badasstechie.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("testuser")
                .email("testuser@example.com")
                .password("password")
                .bio("test bio")
                .image(new byte[0])
                .role(UserRole.USER)
                .created(Instant.now())
                .active(true)
                .build();
    }

    @Test
    void testSignup() {
        UserRequest userRequest = new UserRequest(
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getBio(),
                "",
                user.getRole().name()
        );

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(userRequest.password())).thenReturn(user.getPassword());

        ResponseEntity<UserResponse> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userService.mapUserToResponse(user), response.getBody());
    }

    @Test
    void testDeactivateUser() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(java.util.Optional.ofNullable(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(refreshTokenService).deleteRefreshTokensByUser(any());

        AuthRequest authRequest = new AuthRequest(user.getEmail(), user.getPassword());
        ResponseEntity<String> response = userService.deactivateUser(authRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(user.isActive());
    }
}
