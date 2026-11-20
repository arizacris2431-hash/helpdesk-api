package com.helpDesk.user.controller;

import com.helpDesk.user.dto.RegisterRequest;
import com.helpDesk.user.dto.RegisterResponse;
import com.helpDesk.user.dto.CurrentUserResponse;
import com.helpDesk.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<RegisterResponse> register (@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUserMe () {
        return ResponseEntity.ok(userService.getCurrentUser());
    }
}
