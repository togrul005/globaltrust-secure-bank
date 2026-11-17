package com.globaltrust.bank.domain;

import jakarta.persistence.*;

/**
 * A system user. The password column stores a BCrypt hash, never plaintext.
 * {@code department} and {@code coiClass} are ABAC attributes used by the
 * Chinese Wall policy.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; // BCrypt hash

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String department;

    public AppUser() { }

    public AppUser(String username, String password, String fullName, Role role, String department) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public Role getRole() { return role; }
    public String getDepartment() { return department; }
}
