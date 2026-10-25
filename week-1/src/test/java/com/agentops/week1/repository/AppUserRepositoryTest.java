package com.agentops.week1.repository;

import com.agentops.week1.domain.identity.AppUser;
import com.agentops.week1.domain.tenant.Tenant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class AppUserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("mydatabase")
            .withUsername("my_db_user")
            .withPassword("my_secure_password");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant createTestTenant() {
        Tenant tenant = new Tenant(UUID.randomUUID(), "Test Tenant " + UUID.randomUUID(), "tenant-" + UUID.randomUUID(), "ACTIVE");
        return tenantRepository.save(tenant);
    }

    @Test
    @DisplayName("Should save and read a single user record")
    void shouldSaveAndReadUser() {
        // Given
        Tenant tenant = createTestTenant();
        UUID userId = UUID.randomUUID();
        UUID tenantId = tenant.getId();
        String email = "john-" + UUID.randomUUID() + "@example.com";
        AppUser user = new AppUser(userId, tenantId, email, "hashed_password", "EMPLOYEE", "ACTIVE");

        // When
        AppUser saved = appUserRepository.save(user);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(userId);
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getRole()).isEqualTo("EMPLOYEE");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        Tenant tenant = createTestTenant();
        UUID userId = UUID.randomUUID();
        UUID tenantId = tenant.getId();
        String email = "jane-" + UUID.randomUUID() + "@example.com";
        AppUser user = new AppUser(userId, tenantId, email, "hashed_password", "TENANT_ADMIN", "ACTIVE");
        appUserRepository.save(user);

        // When
        Optional<AppUser> found = appUserRepository.findByEmail(email);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getRole()).isEqualTo("TENANT_ADMIN");
    }

    @Test
    @DisplayName("Should find user by email case insensitive")
    void shouldFindUserByEmailIgnoreCase() {
        // Given
        Tenant tenant = createTestTenant();
        UUID userId = UUID.randomUUID();
        UUID tenantId = tenant.getId();
        String email = "alice-" + UUID.randomUUID() + "@example.com";
        AppUser user = new AppUser(userId, tenantId, email, "hashed_password", "EMPLOYEE", "ACTIVE");
        appUserRepository.save(user);

        // When
        Optional<AppUser> found = appUserRepository.findByEmailIgnoreCase(email.toUpperCase());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should return empty when user email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<AppUser> found = appUserRepository.findByEmail("nonexistent-" + UUID.randomUUID() + "@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find users by tenant id")
    void shouldFindUsersByTenantId() {
        // Given
        Tenant tenant1 = createTestTenant();
        Tenant tenant2 = createTestTenant();
        UUID tenantId = tenant1.getId();
        String email1 = "test1-" + UUID.randomUUID() + "@acme.com";
        String email2 = "test2-" + UUID.randomUUID() + "@acme.com";
        String email3 = "test3-" + UUID.randomUUID() + "@other.com";
        AppUser user1 = new AppUser(UUID.randomUUID(), tenantId, email1, "hash1", "EMPLOYEE", "ACTIVE");
        AppUser user2 = new AppUser(UUID.randomUUID(), tenantId, email2, "hash2", "EMPLOYEE", "ACTIVE");
        AppUser user3 = new AppUser(UUID.randomUUID(), tenant2.getId(), email3, "hash3", "EMPLOYEE", "ACTIVE");
        appUserRepository.saveAll(List.of(user1, user2, user3));

        // When
        List<AppUser> usersByTenant = appUserRepository.findByTenantId(tenantId);

        // Then
        assertThat(usersByTenant).hasSize(2);
        assertThat(usersByTenant).extracting(AppUser::getEmail)
                .containsExactlyInAnyOrder(email1, email2);
    }

    @Test
    @DisplayName("Should find user by tenant id and email")
    void shouldFindUserByTenantIdAndEmail() {
        // Given
        Tenant tenant1 = createTestTenant();
        Tenant tenant2 = createTestTenant();
        UUID tenantId = tenant1.getId();
        String email = "dave-" + UUID.randomUUID() + "@acme.com";
        AppUser user = new AppUser(UUID.randomUUID(), tenantId, email, "hash1", "EMPLOYEE", "ACTIVE");
        AppUser user2 = new AppUser(UUID.randomUUID(), tenant2.getId(), email, "hash2", "EMPLOYEE", "ACTIVE");
        appUserRepository.saveAll(List.of(user, user2));

        // When
        Optional<AppUser> found = appUserRepository.findByTenantIdAndEmail(tenantId, email);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTenantId()).isEqualTo(tenantId);
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Should save and read multiple user records")
    void shouldSaveAndReadMultipleUsers() {
        // Given
        Tenant tenant = createTestTenant();
        UUID tenantId = tenant.getId();
        String email1 = "admin-" + UUID.randomUUID() + "@acme.com";
        String email2 = "alice-" + UUID.randomUUID() + "@acme.com";
        String email3 = "bob-" + UUID.randomUUID() + "@acme.com";
        AppUser user1 = new AppUser(UUID.randomUUID(), tenantId, email1, "hash1", "TENANT_ADMIN", "ACTIVE");
        AppUser user2 = new AppUser(UUID.randomUUID(), tenantId, email2, "hash2", "EMPLOYEE", "ACTIVE");
        AppUser user3 = new AppUser(UUID.randomUUID(), tenantId, email3, "hash3", "EMPLOYEE", "SUSPENDED");
        appUserRepository.saveAll(List.of(user1, user2, user3));

        // When
        List<AppUser> allUsers = appUserRepository.findAll();

        // Then
        assertThat(allUsers).extracting(AppUser::getEmail)
                .contains(email1, email2, email3);
    }

    @Test
    @DisplayName("Should update user status")
    void shouldUpdateUserStatus() {
        // Given
        Tenant tenant = createTestTenant();
        UUID userId = UUID.randomUUID();
        UUID tenantId = tenant.getId();
        String email = "john-" + UUID.randomUUID() + "@example.com";
        AppUser user = new AppUser(userId, tenantId, email, "hashed_password", "EMPLOYEE", "ACTIVE");
        appUserRepository.save(user);

        // When
        user.setStatus("SUSPENDED");
        AppUser updated = appUserRepository.save(user);

        // Then
        assertThat(updated.getStatus()).isEqualTo("SUSPENDED");

        Optional<AppUser> fromDb = appUserRepository.findById(userId);
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getStatus()).isEqualTo("SUSPENDED");
    }
}
