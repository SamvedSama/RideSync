package com.carpool.controller;

import com.carpool.dto.AuthResponse;
import com.carpool.dto.LoginRequest;
import com.carpool.dto.RegisterRequest;
import com.carpool.model.User;
import com.carpool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User(request.getName(), request.getEmail(),
                request.getPassword(), request.getPhone(), request.getRole());
        return ResponseEntity.ok(service.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request.getEmail(), request.getPassword()));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("authentication.principal.userId == #id or hasRole('ADMIN')")
    public ResponseEntity<User> updateProfile(@PathVariable Long id,
                                               @RequestParam String name,
                                               @RequestParam String phone) {
        return ResponseEntity.ok(service.updateProfile(id, name, phone));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProfile(id));
    }
}
