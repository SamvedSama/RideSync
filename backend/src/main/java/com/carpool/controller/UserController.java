package com.carpool.controller;

import com.carpool.dto.RegisterRequest;
import com.carpool.model.User;
import com.carpool.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest request) {

        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone(),
                request.getRole());

        return service.register(user);
    }

    @PostMapping("/login")
    public User login(@RequestParam String email,
            @RequestParam String password) {
        return service.login(email, password);
    }

    @PutMapping("/{id}")
    public User updateProfile(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam String phone) {
        return service.updateProfile(id, name, phone);
    }
}
