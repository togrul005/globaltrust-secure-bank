package com.globaltrust.bank.dto;

import com.globaltrust.bank.domain.AppUser;

public record UserResponse(Long id, String username, String fullName, String role, String department) {
    public static UserResponse of(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name(), u.getDepartment());
    }
}
