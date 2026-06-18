package com.ifsp.userservice.dto;

import com.ifsp.userservice.model.RoleName;

public class UpdateProfileDto {

    private String name;
    private RoleName role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleName getRole() {
        return role;
    }

    public void setRole(RoleName role) {
        this.role = role;
    }
}
