package com.ifsp.userservice.controller;

import com.ifsp.userservice.dto.UpdateProfileDto;
import com.ifsp.userservice.dto.UserProfileDto;
import com.ifsp.userservice.model.Role;
import com.ifsp.userservice.model.User;
import com.ifsp.userservice.security.JwtTokenService;
import com.ifsp.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final com.ifsp.userservice.repository.UserRepository userRepository;
    private final com.ifsp.userservice.repository.RoleRepository roleRepository;
    private final UserService userService;

    public UserController(PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService,
                          com.ifsp.userservice.repository.UserRepository userRepository,
                          com.ifsp.userservice.repository.RoleRepository roleRepository,
                          UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String role = body.getOrDefault("role", "CUSTOMER");
        if (username == null || password == null) return ResponseEntity.badRequest().body(Map.of("error","username/password required"));
        if (userRepository.existsByUsername(username)) return ResponseEntity.status(409).body(Map.of("error","user exists"));
        String hash = passwordEncoder.encode(password);
        User u = new User(username, hash);
        u.setEmail(email);
        Role roleEntity = roleRepository.findByName(role).orElseGet(() -> roleRepository.save(new Role(role)));
        u.getRoles().add(roleEntity);
        userRepository.save(u);
        return ResponseEntity.status(201).body(Map.of("username", username, "role", role));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) return ResponseEntity.badRequest().body(Map.of("error","username/password required"));
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("error","invalid credentials"));
        var user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) return ResponseEntity.status(401).body(Map.of("error","invalid credentials"));
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(name -> name.startsWith("ROLE_") ? name.substring(5) : name)
                .orElse("CUSTOMER");
        String token = jwtTokenService.generateToken(username, roleName);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody UpdateProfileDto dto) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }
        if (dto.getName() == null || dto.getName().isBlank() || dto.getRole() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "name/role required"));
        }

        User updated = userService.updateProfile(authentication.getName(), dto);
        return ResponseEntity.ok(toUserProfileDto(updated));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Map.of("error", "unauthorized"));
        }

        User user = userService.getCurrentProfile(authentication.getName());
        return ResponseEntity.ok(toUserProfileDto(user));
    }

    @GetMapping("/test/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> testCustomer() {
        return ResponseEntity.ok(Map.of("message","ok for customer"));
    }

    private UserProfileDto toUserProfileDto(User user) {
        String role = user.getRoles().stream().findFirst().map(Role::getName).orElse("CUSTOMER");
        return new UserProfileDto(user.getUserUuid(), user.getEmail(), user.getName(), role);
    }
}
