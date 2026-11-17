package com.globaltrust.bank.security;

import com.globaltrust.bank.domain.AppUser;
import com.globaltrust.bank.repo.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/** Loads users from the database and exposes their role as a {@code ROLE_*} authority. */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public JpaUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))
                .build();
    }
}
