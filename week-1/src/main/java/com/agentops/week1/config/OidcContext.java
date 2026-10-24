package com.agentops.week1.config;

import com.agentops.week1.common.RoleEnum;
import com.agentops.week1.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class OidcContext {

    /**
     * Get the current authenticated user's JWT.
     * @return the JWT or null if not authenticated
     */
    public Jwt getCurrentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    /**
     * Get the current user's email.
     */
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser.getEmail();
        }
        Jwt jwt = getCurrentJwt();
        if (jwt != null && jwt.hasClaim("email")) {
            return jwt.getClaim("email");
        }
        return auth.getName();
    }

    /**
     * Get the current user's unique identifier.
     */
    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser.getId() != null ? currentUser.getId().toString() : null;
        }
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            return jwt.getSubject();
        }
        return auth.getName();
    }

    /**
     * Get all roles as RoleEnum values.
     */
    public List<RoleEnum> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return List.of();
        Collection<? extends org.springframework.security.core.GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream()
            .map(a -> {
                String role = a.getAuthority().replaceFirst("^ROLE_", "");
                try {
                    return RoleEnum.valueOf(role);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Check if the current user has a specific role.
     */
    public boolean hasRole(RoleEnum role) {
        return getCurrentUserRoles().contains(role);
    }

    /**
     * Check if the current user has any of the specified roles.
     */
    public boolean hasAnyRole(RoleEnum... roles) {
        List<RoleEnum> userRoles = getCurrentUserRoles();
        for (RoleEnum role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a specific claim from the current JWT.
     */
    public Object getClaim(String claimName) {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) return null;
        return jwt.getClaim(claimName);
    }

    /**
     * Get all claims from the current JWT.
     */
    public Map<String, Object> getAllClaims() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) return Map.of();
        return jwt.getClaims();
    }
}
