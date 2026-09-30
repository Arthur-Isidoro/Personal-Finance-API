package com.arthurisidoro.personal_finance_api.controller;

import com.arthurisidoro.personal_finance_api.dto.request.UpdatePreferencesRequest;
import com.arthurisidoro.personal_finance_api.dto.response.UserPreferencesResponse;
import com.arthurisidoro.personal_finance_api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> getPreferences() {
        return ResponseEntity.ok(userService.getPreferences());
    }

    @PutMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(userService.updatePreferences(request));
    }
}