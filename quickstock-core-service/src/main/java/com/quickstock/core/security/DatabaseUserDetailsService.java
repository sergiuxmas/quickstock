package com.quickstock.core.security;

import com.quickstock.core.domain.AppUser;
import com.quickstock.core.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String role = normalizeRole(user.getRole());
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_CUSTOMER";
        }
        String normalized = role.trim().toUpperCase();
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }
}

