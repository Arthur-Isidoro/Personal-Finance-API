package com.arthurisidoro.personal_finance_api.controller;

import com.arthurisidoro.personal_finance_api.dto.request.LoginRequest;
import com.arthurisidoro.personal_finance_api.dto.request.RegisterRequest;
import com.arthurisidoro.personal_finance_api.dto.response.LoginResponse;
import com.arthurisidoro.personal_finance_api.dto.response.UserResponse;
import com.arthurisidoro.personal_finance_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}