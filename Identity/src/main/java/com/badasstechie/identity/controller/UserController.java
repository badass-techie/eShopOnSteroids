package com.badasstechie.identity.controller;

import com.badasstechie.identity.dto.AuthRequest;
import com.badasstechie.identity.dto.UserRequest;
import com.badasstechie.identity.dto.UserResponse;
import com.badasstechie.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/identity/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest userRequest){
        return userService.createUser(userRequest);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return userService.getUser(id);
    }

    @DeleteMapping
    public ResponseEntity<String> deactivateUser(@RequestBody AuthRequest authRequest){
        return userService.deactivateUser(authRequest);
    }
}
