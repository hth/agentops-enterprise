package com.agentops.repository;

import com.agentops.domain.conversation.Conversation;
import com.agentops.domain.identity.AppUser;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Transactional
class ConversationRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("mydatabase")
            .withUsername("my_db_user")
            .withPassword("my_secure_password");

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private Tenant createTestTenant() {
        Tenant tenant = new Tenant(UUID.randomUUID(), "Test Tenant " + UUID.randomUUID(), "tenant-" + UUID.randomUUID(), "ACTIVE");
        return tenantRepository.save(tenant);
    }

    private AppUser createTestUser(UUID tenantId) {
        AppUser user = new AppUser(UUID.randomUUID(), tenantId, "user-" + UUID.randomUUID() + "@example.com", "hash", "EMPLOYEE", "ACTIVE");
        return appUserRepository.save(user);
    }

    @Test
    @DisplayName("Should save and read a single conversation record")
    void shouldSaveAndReadConversation() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        UUID userId = user.getId();
        Conversation conversation = Conversation.create(tenantId, userId, "Test Conversation");

        // When
        Conversation saved = conversationRepository.save(conversation);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getTitle()).isEqualTo("Test Conversation");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find conversations by tenant id")
    void shouldFindConversationsByTenantId() {
        // Given
        Tenant tenant1 = createTestTenant();
        Tenant tenant2 = createTestTenant();
        AppUser user1 = createTestUser(tenant1.getId());
        AppUser user2 = createTestUser(tenant2.getId());
        UUID tenantId1 = tenant1.getId();
        UUID tenantId2 = tenant2.getId();
        Conversation conv1 = Conversation.create(tenantId1, user1.getId(), "Conv 1");
        Conversation conv2 = Conversation.create(tenantId1, user1.getId(), "Conv 2");
        Conversation conv3 = Conversation.create(tenantId2, user2.getId(), "Conv 3");
        conversationRepository.saveAll(List.of(conv1, conv2, conv3));

        // When
        List<Conversation> tenant1Convs = conversationRepository.findByTenantId(tenantId1);

        // Then
        assertThat(tenant1Convs).hasSize(2);
        assertThat(tenant1Convs).extracting(Conversation::getTitle)
                .containsExactlyInAnyOrder("Conv 1", "Conv 2");
    }

    @Test
    @DisplayName("Should find conversations by user id")
    void shouldFindConversationsByUserId() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user1 = createTestUser(tenant.getId());
        AppUser user2 = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        UUID userId1 = user1.getId();
        UUID userId2 = user2.getId();
        Conversation conv1 = Conversation.create(tenantId, userId1, "User1 Conv 1");
        Conversation conv2 = Conversation.create(tenantId, userId1, "User1 Conv 2");
        Conversation conv3 = Conversation.create(tenantId, userId2, "User2 Conv");
        conversationRepository.saveAll(List.of(conv1, conv2, conv3));

        // When
        List<Conversation> user1Convs = conversationRepository.findByUserId(userId1);

        // Then
        assertThat(user1Convs).hasSize(2);
        assertThat(user1Convs).extracting(Conversation::getTitle)
                .containsExactlyInAnyOrder("User1 Conv 1", "User1 Conv 2");
    }

    @Test
    @DisplayName("Should find conversations by tenant id and user id")
    void shouldFindConversationsByTenantIdAndUserId() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user1 = createTestUser(tenant.getId());
        AppUser user2 = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        UUID userId = user1.getId();
        Conversation conv1 = Conversation.create(tenantId, userId, "Shared Conv 1");
        Conversation conv2 = Conversation.create(tenantId, userId, "Shared Conv 2");
        Conversation conv3 = Conversation.create(tenantId, user2.getId(), "Other User Conv");
        conversationRepository.saveAll(List.of(conv1, conv2, conv3));

        // When
        List<Conversation> sharedConvs = conversationRepository.findByTenantIdAndUserId(tenantId, userId);

        // Then
        assertThat(sharedConvs).hasSize(2);
        assertThat(sharedConvs).extracting(Conversation::getTitle)
                .containsExactlyInAnyOrder("Shared Conv 1", "Shared Conv 2");
    }

    @Test
    @DisplayName("Should save and read multiple conversation records")
    void shouldSaveAndReadMultipleConversations() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        UUID userId = user.getId();
        Conversation conv1 = Conversation.create(tenantId, userId, "Project Discussion");
        Conversation conv2 = Conversation.create(tenantId, userId, "Bug Report");
        Conversation conv3 = Conversation.create(tenantId, userId, "Feature Request");
        conversationRepository.saveAll(List.of(conv1, conv2, conv3));

        // When
        List<Conversation> allConvs = conversationRepository.findAll();

        // Then
        assertThat(allConvs).extracting(Conversation::getTitle)
                .contains("Project Discussion", "Bug Report", "Feature Request");
    }

    @Test
    @DisplayName("Should update conversation title")
    void shouldUpdateConversationTitle() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        UUID userId = user.getId();
        Conversation conversation = Conversation.create(tenantId, userId, "Old Title");
        conversationRepository.save(conversation);

        // When
        conversation.setTitle("New Title");
        Conversation updated = conversationRepository.save(conversation);

        // Then
        assertThat(updated.getTitle()).isEqualTo("New Title");

        Conversation fromDb = conversationRepository.findById(updated.getId()).orElseThrow();
        assertThat(fromDb.getTitle()).isEqualTo("New Title");
    }
}
