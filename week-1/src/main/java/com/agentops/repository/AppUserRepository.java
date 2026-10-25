package com.agentops.repository;

import com.agentops.domain.identity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByEmailIgnoreCase(String email);
    List<AppUser> findByTenantId(UUID tenantId);
    Optional<AppUser> findByTenantIdAndEmail(UUID tenantId, String email);
}
