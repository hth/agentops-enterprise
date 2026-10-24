package com.agentops.week1.security;

import com.agentops.week1.domain.identity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CurrentUser implements UserDetails {

    private final AppUser appUser;
    private final Collection<? extends GrantedAuthority> authorities;

    public CurrentUser(AppUser appUser) {
        this.appUser = appUser;
        String roleName = appUser.getRole();
        if (roleName != null && !roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }
        this.authorities = roleName != null
                ? List.of(new SimpleGrantedAuthority(roleName))
                : List.of();
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public UUID getId() {
        return appUser.getId();
    }

    public UUID getTenantId() {
        return appUser.getTenantId();
    }

    public String getEmail() {
        return appUser.getEmail();
    }

    public String getRole() {
        return appUser.getRole();
    }

    public String getStatus() {
        return appUser.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return appUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return appUser.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return appUser.getStatus() == null || "ACTIVE".equalsIgnoreCase(appUser.getStatus());
    }
}
