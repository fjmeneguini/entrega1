package com.ifsp.userservice.controller;

import com.ifsp.userservice.dto.EmailDto;
import com.ifsp.userservice.model.Role;
import com.ifsp.userservice.model.User;
import com.ifsp.userservice.producer.UserProducer;
import com.ifsp.userservice.repository.RoleRepository;
import com.ifsp.userservice.repository.UserRepository;
import com.ifsp.userservice.service.CodigoCacheService;
import com.ifsp.userservice.security.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoCacheService codigoCacheService;
    private final UserProducer userProducer;
    private final JwtTokenService jwtTokenService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          CodigoCacheService codigoCacheService,
                          UserProducer userProducer,
                          JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.codigoCacheService = codigoCacheService;
        this.userProducer = userProducer;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/request-code")
    public ResponseEntity<?> requestCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email required"));
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> createTemporaryUser(email));
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        codigoCacheService.putCode(email, code);

        EmailDto emailDto = new EmailDto(
                email,
                "Seu código de acesso",
                "Seu código de acesso é: " + code,
                user.getUserUuid()
        );
        userProducer.sendEmail(emailDto);

        return ResponseEntity.ok(Map.of("message", "code requested"));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email/code required"));
        }

        return codigoCacheService.getValidCode(email)
                .filter(code::equals)
                .map(found -> {
                    codigoCacheService.removeCode(email);
                User user = userRepository.findByEmail(email).orElseGet(() -> createTemporaryUser(email));
                String roleName = user.getRoles().stream()
                    .findFirst()
                    .map(Role::getName)
                    .map(name -> name.startsWith("ROLE_") ? name.substring(5) : name)
                    .orElse("CUSTOMER");
                    String principal = user.getEmail() != null ? user.getEmail() : user.getUsername();
                    String token = jwtTokenService.generateToken(principal, roleName);
                return ResponseEntity.ok(Map.of("message", "code verified", "token", token));
                })
                .orElseGet(() -> ResponseEntity.status(400).body(Map.of("error", "invalid or expired code")));
    }

    private User createTemporaryUser(String email) {
        String randomPassword = passwordEncoder.encode(Long.toHexString(secureRandom.nextLong()));
        User user = new User(email, randomPassword);
        user.setEmail(email);
        Role role = roleRepository.findByName("CUSTOMER")
            .orElseGet(() -> roleRepository.save(new Role("CUSTOMER")));
        user.getRoles().add(role);
        return userRepository.save(user);
    }
}
