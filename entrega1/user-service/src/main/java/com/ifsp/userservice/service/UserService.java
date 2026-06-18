package com.ifsp.userservice.service;

import com.ifsp.userservice.dto.UpdateProfileDto;
import com.ifsp.userservice.model.Role;
import com.ifsp.userservice.model.User;
import com.ifsp.userservice.repository.RoleRepository;
import com.ifsp.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User updateProfile(String principal, UpdateProfileDto dto) {
        User user = userRepository.findByEmail(principal)
                .or(() -> userRepository.findByUsername(principal))
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        user.setName(dto.getName());

        String roleName = dto.getRole().name();
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        LinkedHashSet<Role> roles = new LinkedHashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User getCurrentProfile(String principal) {
        return userRepository.findByEmail(principal)
                .or(() -> userRepository.findByUsername(principal))
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
    }
}
