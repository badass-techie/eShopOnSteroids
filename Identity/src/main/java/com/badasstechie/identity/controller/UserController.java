package com.badasstechie.identity.controller;

import com.badasstechie.identity.dto.AuthRequest;
import com.badasstechie.identity.dto.UserRequest;
import com.badasstechie.identity.dto.UserResponse;
import com.badasstechie.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register an account")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest){
        return userService.createUser(userRequest);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    public UserResponse getUser(@PathVariable Long id){
        return userService.getUser(id);
    }

    @DeleteMapping
    @Operation(summary = "Deactivate your account")
    public ResponseEntity<String> deactivateUser(@RequestBody AuthRequest authRequest){
        return userService.deactivateUser(authRequest);
    }
}
