package com.badasstechie.identity.service;

import com.badasstechie.identity.dto.SignupRequest;
import com.badasstechie.identity.dto.UserResponse;
import com.badasstechie.identity.model.User;
import com.badasstechie.identity.model.UserRole;
import com.badasstechie.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse mapUserToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                new String(user.getImage() != null ? user.getImage() : new byte[0]),
                user.getRole().name(),
                user.getCreated(),
                user.isActive()
        );
    }

    public ResponseEntity<UserResponse> signup(SignupRequest signupRequest) {
        User user = userRepository.save(
                User.builder()
                .username(signupRequest.username())
                .email(signupRequest.email())
                .password(signupRequest.password())
                .bio(signupRequest.bio())
                .image(signupRequest.image().getBytes())
                .role(UserRole.valueOf(signupRequest.role()))
                .created(Instant.now())
                .active(true)
                .build()
        );

        return new ResponseEntity<>(mapUserToResponse(user), HttpStatus.CREATED);
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapUserToResponse(user);
    }

    public ResponseEntity<String> deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
