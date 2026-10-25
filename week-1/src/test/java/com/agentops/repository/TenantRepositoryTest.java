package com.agentops.repository;

import com.agentops.domain.tenant.Tenant;
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
class TenantRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("mydatabase")
            .withUsername("my_db_user")
            .withPassword("my_secure_password");

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    @DisplayName("Should save and read a single tenant record")
    void shouldSaveAndReadTenant() {
        // Given
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant(tenantId, "Test Acme Corp", "test-acme-" + UUID.randomUUID(), "ACTIVE");

        // When
        Tenant saved = tenantRepository.save(tenant);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(tenantId);
        assertThat(saved.getName()).isEqualTo("Test Acme Corp");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find tenant by slug")
    void shouldFindTenantBySlug() {
        // Given
        UUID tenantId = UUID.randomUUID();
        String slug = "test-globex-" + UUID.randomUUID();
        Tenant tenant = new Tenant(tenantId, "Test Globex Inc", slug, "ACTIVE");
        tenantRepository.save(tenant);

        // When
        Optional<Tenant> found = tenantRepository.findBySlug(slug);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Globex Inc");
        assertThat(found.get().getSlug()).isEqualTo(slug);
    }

    @Test
    @DisplayName("Should return empty when tenant slug not found")
    void shouldReturnEmptyWhenSlugNotFound() {
        // When
        Optional<Tenant> found = tenantRepository.findBySlug("nonexistent-" + UUID.randomUUID());

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should save and read multiple tenant records")
    void shouldSaveAndReadMultipleTenants() {
        // Given
        String slug1 = "test-acme-" + UUID.randomUUID();
        String slug2 = "test-globex-" + UUID.randomUUID();
        String slug3 = "test-initech-" + UUID.randomUUID();
        Tenant tenant1 = new Tenant(UUID.randomUUID(), "Test Acme Corp", slug1, "ACTIVE");
        Tenant tenant2 = new Tenant(UUID.randomUUID(), "Test Globex Inc", slug2, "SUSPENDED");
        Tenant tenant3 = new Tenant(UUID.randomUUID(), "Test Initech", slug3, "ACTIVE");
        tenantRepository.saveAll(List.of(tenant1, tenant2, tenant3));

        // When
        List<Tenant> allTenants = tenantRepository.findAll();

        // Then
        assertThat(allTenants).extracting(Tenant::getSlug)
                .contains(slug1, slug2, slug3);
    }

    @Test
    @DisplayName("Should update tenant status")
    void shouldUpdateTenantStatus() {
        // Given
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = new Tenant(tenantId, "Test Acme Corp", "test-acme-" + UUID.randomUUID(), "ACTIVE");
        tenantRepository.save(tenant);

        // When
        tenant.setStatus("SUSPENDED");
        Tenant updated = tenantRepository.save(tenant);

        // Then
        assertThat(updated.getStatus()).isEqualTo("SUSPENDED");

        // Verify from database
        Optional<Tenant> fromDb = tenantRepository.findById(tenantId);
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getStatus()).isEqualTo("SUSPENDED");
    }
}
