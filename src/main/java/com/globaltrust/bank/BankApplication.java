package com.globaltrust.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GlobalTrust Secure Bank — security prototype.
 *
 * Demonstrates, in a realistic banking workflow:
 *  - Authentication (HTTP Basic, BCrypt, JPA-backed users)
 *  - Authorization via RBAC ({@code @PreAuthorize}) and ABAC (Chinese Wall)
 *  - The Clark-Wilson integrity model: well-formed transactions, separation of
 *    duty (maker-checker) and mandatory audit logging
 *  - The Chinese Wall / Brewer-Nash model: dynamic conflict-of-interest barrier
 */
@SpringBootApplication
public class BankApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }
}
