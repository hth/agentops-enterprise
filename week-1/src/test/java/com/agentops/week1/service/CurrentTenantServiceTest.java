package com.agentops.week1.service;

import com.agentops.week1.domain.identity.AppUser;
import com.agentops.week1.security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentTenantServiceTest {

    private CurrentTenantService currentTenantService;

    @BeforeEach
    void setUp() {
        currentTenantService = new CurrentTenantService();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return tenant ID when authenticated with CurrentUser")
    void shouldReturnTenantIdWhenAuthenticatedWithCurrentUser() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        AppUser appUser = new AppUser(userId, tenantId, "user@example.com", "hash", "EMPLOYEE", "ACTIVE");
        CurrentUser currentUser = new CurrentUser(appUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<UUID> result = currentTenantService.getCurrentTenantId();

        assertThat(result).isPresent().contains(tenantId);
    }

    @Test
    @DisplayName("Should return empty tenant ID when SecurityContext is empty")
    void shouldReturnEmptyTenantIdWhenNoAuthentication() {
        Optional<UUID> result = currentTenantService.getCurrentTenantId();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty tenant ID when principal is not CurrentUser")
    void shouldReturnEmptyTenantIdWhenPrincipalIsNotCurrentUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<UUID> result = currentTenantService.getCurrentTenantId();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty tenant ID when CurrentUser has null tenantId")
    void shouldReturnEmptyTenantIdWhenTenantIdIsNull() {
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.getTenantId()).thenReturn(null);

        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<UUID> result = currentTenantService.getCurrentTenantId();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return CurrentUser when authenticated")
    void shouldReturnCurrentUserWhenAuthenticated() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        AppUser appUser = new AppUser(userId, tenantId, "user@example.com", "hash", "EMPLOYEE", "ACTIVE");
        CurrentUser currentUser = new CurrentUser(appUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<CurrentUser> result = currentTenantService.getCurrentUser();

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(currentUser);
        assertThat(result.get().getUsername()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("Should return empty CurrentUser when not authenticated")
    void shouldReturnEmptyCurrentUserWhenNoAuthentication() {
        Optional<CurrentUser> result = currentTenantService.getCurrentUser();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty CurrentUser when principal is not CurrentUser instance")
    void shouldReturnEmptyCurrentUserWhenPrincipalIsNotCurrentUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken("justAUsername", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<CurrentUser> result = currentTenantService.getCurrentUser();

        assertThat(result).isEmpty();
    }
}
