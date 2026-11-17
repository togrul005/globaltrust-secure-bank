package com.globaltrust.bank.web;

import com.globaltrust.bank.dto.CreateUserRequest;
import com.globaltrust.bank.dto.UserResponse;
import com.globaltrust.bank.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminService admin;

    public AdminController(AdminService admin) {
        this.admin = admin;
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest req) {
        return admin.createUser(req);
    }

    @GetMapping
    public List<UserResponse> list() {
        return admin.listUsers();
    }
}
