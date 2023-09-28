package com.badasstechie.identity.service;

import com.badasstechie.identity.dto.AuthRequest;
import com.badasstechie.identity.dto.UserRequest;
import com.badasstechie.identity.dto.UserResponse;
import com.badasstechie.identity.model.User;
import com.badasstechie.identity.model.UserRole;
import com.badasstechie.identity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy   // Lazy to avoid circular dependency
    UserService(UserRepository userRepository, RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User with name/email %s not found", username)));
    }

    public UserResponse mapUserToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                new String(user.getImage() != null ? user.getImage() : new byte[0]),
                user.getRole().name(),
                user.getCreated(),
                user.isActive()
        );
    }

    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        if (UserRole.valueOf(userRequest.role()) == UserRole.ADMIN)
            throw new RuntimeException("Cannot manually create admin user");

        User user = userRepository.save(
                User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password()))
                .bio(userRequest.bio())
                .image(userRequest.image() != null ? userRequest.image().getBytes() : new byte[0])
                .role(UserRole.valueOf(userRequest.role()))
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

    public ResponseEntity<String> deactivateUser(AuthRequest authRequest) {
        // The idea is that the user has to reenter their password to deactivate their account hence no use of tokens
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password()));

            Optional<User> userOptional = userRepository.findByEmail(authRequest.email());
            if(userOptional.isEmpty())
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

            User user = userOptional.get();
            user.setActive(false);
            userRepository.save(user);
            refreshTokenService.deleteRefreshTokensByUser(user.getEmail());
            return new ResponseEntity<>("Account deactivated", HttpStatus.NO_CONTENT);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Incorrect username or password", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
