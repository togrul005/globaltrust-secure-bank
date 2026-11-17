package com.globaltrust.bank.dto;

import com.globaltrust.bank.domain.Role;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String fullName,
        @NotNull Role role,
        String department
) { }
