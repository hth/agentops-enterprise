package com.agentops.week1.config;

import com.agentops.week1.common.RoleEnum;
import com.agentops.week1.domain.identity.AppUser;
import com.agentops.week1.security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OidcContextTest {

    private OidcContext oidcContext;

    @BeforeEach
    void setUp() {
        oidcContext = new OidcContext();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should extract JWT when principal is Jwt")
    void shouldExtractJwtWhenPrincipalIsJwt() {
        Jwt jwt = new Jwt("tokenValue", Instant.now(), Instant.now().plusSeconds(3600),
                Map.of("alg", "none"), Map.of("sub", "user-123", "email", "jwt@example.com"));
        Authentication auth = new UsernamePasswordAuthenticationToken(jwt, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(oidcContext.getCurrentJwt()).isEqualTo(jwt);
        assertThat(oidcContext.getCurrentUserEmail()).isEqualTo("jwt@example.com");
        assertThat(oidcContext.getCurrentUserId()).isEqualTo("user-123");
        assertThat(oidcContext.getClaim("email")).isEqualTo("jwt@example.com");
        assertThat(oidcContext.getAllClaims()).containsEntry("email", "jwt@example.com");
    }

    @Test
    @DisplayName("Should extract user details when principal is CurrentUser")
    void shouldExtractDetailsWhenPrincipalIsCurrentUser() {
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        AppUser appUser = new AppUser(userId, tenantId, "current@example.com", "hash", "PLATFORM_ADMIN", "ACTIVE");
        CurrentUser currentUser = new CurrentUser(appUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, null, currentUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(oidcContext.getCurrentJwt()).isNull();
        assertThat(oidcContext.getCurrentUserEmail()).isEqualTo("current@example.com");
        assertThat(oidcContext.getCurrentUserId()).isEqualTo(userId.toString());
        assertThat(oidcContext.getCurrentUserRoles()).containsExactly(RoleEnum.PLATFORM_ADMIN);
        assertThat(oidcContext.hasRole(RoleEnum.PLATFORM_ADMIN)).isTrue();
        assertThat(oidcContext.hasRole(RoleEnum.EMPLOYEE)).isFalse();
        assertThat(oidcContext.hasAnyRole(RoleEnum.EMPLOYEE, RoleEnum.PLATFORM_ADMIN)).isTrue();
        assertThat(oidcContext.hasAnyRole(RoleEnum.TENANT_ADMIN)).isFalse();
    }

    @Test
    @DisplayName("Should return null/empty when not authenticated")
    void shouldReturnNullOrEmptyWhenUnauthenticated() {
        assertThat(oidcContext.getCurrentJwt()).isNull();
        assertThat(oidcContext.getCurrentUserEmail()).isNull();
        assertThat(oidcContext.getCurrentUserId()).isNull();
        assertThat(oidcContext.getCurrentUserRoles()).isEmpty();
        assertThat(oidcContext.hasRole(RoleEnum.EMPLOYEE)).isFalse();
        assertThat(oidcContext.hasAnyRole(RoleEnum.EMPLOYEE)).isFalse();
        assertThat(oidcContext.getClaim("any")).isNull();
        assertThat(oidcContext.getAllClaims()).isEmpty();
    }

    @Test
    @DisplayName("Should ignore unknown roles gracefully")
    void shouldIgnoreUnknownRoles() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_UNKNOWN_ROLE"), new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(oidcContext.getCurrentUserRoles()).containsExactly(RoleEnum.EMPLOYEE);
    }
}
