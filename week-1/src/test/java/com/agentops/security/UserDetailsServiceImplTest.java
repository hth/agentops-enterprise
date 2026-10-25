package com.agentops.security;

import com.agentops.domain.identity.AppUser;
import com.agentops.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private UUID tenantId;
    private AppUser appUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        appUser = new AppUser(userId, tenantId, "alice@example.com", "encodedPassword", "TENANT_ADMIN", "ACTIVE");
    }

    @Test
    @DisplayName("Should load user by case-insensitive email")
    void shouldLoadUserByEmailIgnoreCase() {
        String email = "Alice@Example.COM";
        when(appUserRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(appUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("alice@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_TENANT_ADMIN");

        verify(appUserRepository, times(1)).findByEmailIgnoreCase(email);
        verify(appUserRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should fall back to case-sensitive email lookup when case-insensitive returns empty")
    void shouldFallbackToExactEmailLookup() {
        String email = "alice@example.com";
        when(appUserRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail(email)).thenReturn(Optional.of(appUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("alice@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");

        verify(appUserRepository, times(1)).findByEmailIgnoreCase(email);
        verify(appUserRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user is not found")
    void shouldThrowUsernameNotFoundExceptionWhenUserNotFound() {
        String email = "unknown@example.com";
        when(appUserRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.empty());
        when(appUserRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);

        verify(appUserRepository, times(1)).findByEmailIgnoreCase(email);
        verify(appUserRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Should correctly reflect DISABLED status as disabled UserDetails")
    void shouldReflectDisabledStatus() {
        AppUser disabledUser = new AppUser(userId, tenantId, "disabled@example.com", "hash", "EMPLOYEE", "DISABLED");
        when(appUserRepository.findByEmailIgnoreCase("disabled@example.com")).thenReturn(Optional.of(disabledUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername("disabled@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();
    }
}
