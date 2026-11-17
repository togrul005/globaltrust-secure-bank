package com.globaltrust.bank.service;

import com.globaltrust.bank.audit.AuditService;
import com.globaltrust.bank.domain.AppUser;
import com.globaltrust.bank.dto.CreateUserRequest;
import com.globaltrust.bank.dto.UserResponse;
import com.globaltrust.bank.exception.ConflictException;
import com.globaltrust.bank.repo.AppUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User administration. ADMIN can create and list users but, by design, has no
 * authority over money movement or approvals — a deliberate separation of duty
 * between the administrative and business functions.
 */
@Service
public class AdminService {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AuditService audit;

    public AdminService(AppUserRepository users, PasswordEncoder encoder, AuditService audit) {
        this.users = users;
        this.encoder = encoder;
        this.audit = audit;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse createUser(CreateUserRequest req) {
        if (users.existsByUsername(req.username())) {
            throw new ConflictException("Username already exists: " + req.username());
        }
        AppUser u = new AppUser(req.username(), encoder.encode(req.password()),
                req.fullName(), req.role(), req.department());
        users.save(u);
        audit.record("CREATE_USER", "USER", req.username(),
                "provisioned with role " + req.role(), "EXECUTED");
        return UserResponse.of(u);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return users.findAll().stream().map(UserResponse::of).toList();
    }
}
