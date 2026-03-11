package com.quickstock.core.service;

import com.quickstock.core.domain.AppUser;
import com.quickstock.core.repository.UserRepository;
import com.quickstock.core.security.DatabaseUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String DEFAULT_PASSWORD_HASH = "123456";

    @Mock
    private UserRepository userRepository;

    private DatabaseUserDetailsService databaseUserDetailsService;

    @BeforeEach
    void setUp() {
        databaseUserDetailsService = new DatabaseUserDetailsService(userRepository);
    }

    @Test
    @DisplayName("loads user with normalized ROLE_ADMIN and account flags")
    void loadUserByUsername_returnsExpectedUserDetailsForAdminRole() {
        String email = "admin@gmail.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(buildUser(email, "admin")));

        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(email);

        assertAll(
                () -> assertEquals(email, userDetails.getUsername()),
                () -> assertEquals(1, userDetails.getAuthorities().size()),
                () -> assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))),
                () -> assertTrue(userDetails.isEnabled()),
                () -> assertTrue(userDetails.isAccountNonExpired()),
                () -> assertTrue(userDetails.isAccountNonLocked()),
                () -> assertTrue(userDetails.isCredentialsNonExpired())
        );
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("normalizes non-prefixed role to ROLE_<UPPERCASE>")
    void loadUserByUsername_normalizesCustomRole() {
        String email = "operator@gmail.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(buildUser(email, "operator")));

        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(email);

        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_OPERATOR")));
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("defaults to ROLE_CUSTOMER when role is null")
    void loadUserByUsername_defaultsToCustomerWhenRoleIsNull() {
        String email = "null-role@gmail.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(buildUser(email, null)));

        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(email);

        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("defaults to ROLE_CUSTOMER when role is blank")
    void loadUserByUsername_defaultsToCustomerWhenRoleIsBlank() {
        String email = "blank-role@gmail.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(buildUser(email, "   ")));

        UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(email);

        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    @Test
    @DisplayName("throws UsernameNotFoundException when user does not exist")
    void loadUserByUsername_throwsWhenUserMissing() {
        String email = "missing@gmail.com";
        when(userRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> databaseUserDetailsService.loadUserByUsername(email));
        verify(userRepository, times(1)).findByEmailIgnoreCase(email);
    }

    private AppUser buildUser(String email, String role) {
        AppUser appUser = new AppUser();
        appUser.setId(USER_ID);
        appUser.setEmail(email);
        appUser.setRole(role);
        appUser.setPasswordHash(DEFAULT_PASSWORD_HASH);
        return appUser;
    }
}
