package com.badasstechie.identity.controller;

import com.badasstechie.identity.dto.SignupRequest;
import com.badasstechie.identity.dto.UserResponse;
import com.badasstechie.identity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest signupRequest){
        return userService.signup(signupRequest);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id){
        return userService.getUser(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deactivateUser(@PathVariable Long id){
        return userService.deactivateUser(id);
    }
}
