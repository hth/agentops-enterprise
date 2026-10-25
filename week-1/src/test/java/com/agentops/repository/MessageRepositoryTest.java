package com.agentops.repository;

import com.agentops.domain.conversation.Conversation;
import com.agentops.domain.conversation.Message;
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
class MessageRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("mydatabase")
            .withUsername("my_db_user")
            .withPassword("my_secure_password");

    @Autowired
    private MessageRepository messageRepository;

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

    private Conversation createTestConversation(UUID tenantId, UUID userId) {
        Conversation conversation = Conversation.create(tenantId, userId, "Test Conversation " + UUID.randomUUID());
        return conversationRepository.save(conversation);
    }

    @Test
    @DisplayName("Should save and read a single message record")
    void shouldSaveAndReadMessage() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        Conversation conversation = createTestConversation(tenant.getId(), user.getId());
        UUID tenantId = tenant.getId();
        UUID conversationId = conversation.getId();
        Message message = Message.create(tenantId, conversationId, "USER", "Hello, world!");

        // When
        Message saved = messageRepository.save(message);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
        assertThat(saved.getConversationId()).isEqualTo(conversationId);
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getContent()).isEqualTo("Hello, world!");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find messages by conversation id")
    void shouldFindMessagesByConversationId() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        Conversation conv1 = createTestConversation(tenantId, user.getId());
        Conversation conv2 = createTestConversation(tenantId, user.getId());
        UUID convId1 = conv1.getId();
        UUID convId2 = conv2.getId();
        Message msg1 = Message.create(tenantId, convId1, "USER", "Message 1");
        Message msg2 = Message.create(tenantId, convId1, "ASSISTANT", "Response 1");
        Message msg3 = Message.create(tenantId, convId2, "USER", "Message 3");
        messageRepository.saveAll(List.of(msg1, msg2, msg3));

        // When
        List<Message> conv1Messages = messageRepository.findByConversationId(convId1);

        // Then
        assertThat(conv1Messages).hasSize(2);
        assertThat(conv1Messages).extracting(Message::getContent)
                .containsExactlyInAnyOrder("Message 1", "Response 1");
    }

    @Test
    @DisplayName("Should find messages by tenant id")
    void shouldFindMessagesByTenantId() {
        // Given
        Tenant tenant1 = createTestTenant();
        Tenant tenant2 = createTestTenant();
        AppUser user1 = createTestUser(tenant1.getId());
        AppUser user2 = createTestUser(tenant2.getId());
        Conversation conv1 = createTestConversation(tenant1.getId(), user1.getId());
        Conversation conv2 = createTestConversation(tenant2.getId(), user2.getId());
        UUID tenantId1 = tenant1.getId();
        UUID tenantId2 = tenant2.getId();
        Message msg1 = Message.create(tenantId1, conv1.getId(), "USER", "Tenant1 Msg 1");
        Message msg2 = Message.create(tenantId1, conv1.getId(), "ASSISTANT", "Tenant1 Msg 2");
        Message msg3 = Message.create(tenantId2, conv2.getId(), "USER", "Tenant2 Msg");
        messageRepository.saveAll(List.of(msg1, msg2, msg3));

        // When
        List<Message> tenant1Messages = messageRepository.findByTenantId(tenantId1);

        // Then
        assertThat(tenant1Messages).hasSize(2);
        assertThat(tenant1Messages).extracting(Message::getContent)
                .containsExactlyInAnyOrder("Tenant1 Msg 1", "Tenant1 Msg 2");
    }

    @Test
    @DisplayName("Should find messages by tenant id and conversation id")
    void shouldFindMessagesByTenantIdAndConversationId() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        Conversation conv1 = createTestConversation(tenantId, user.getId());
        Conversation conv2 = createTestConversation(tenantId, user.getId());
        UUID convId1 = conv1.getId();
        UUID convId2 = conv2.getId();
        Message msg1 = Message.create(tenantId, convId1, "USER", "Conv1 Msg 1");
        Message msg2 = Message.create(tenantId, convId1, "ASSISTANT", "Conv1 Msg 2");
        Message msg3 = Message.create(tenantId, convId2, "USER", "Conv2 Msg");
        messageRepository.saveAll(List.of(msg1, msg2, msg3));

        // When
        List<Message> filteredMessages = messageRepository.findByTenantIdAndConversationId(tenantId, convId1);

        // Then
        assertThat(filteredMessages).hasSize(2);
        assertThat(filteredMessages).extracting(Message::getContent)
                .containsExactlyInAnyOrder("Conv1 Msg 1", "Conv1 Msg 2");
    }

    @Test
    @DisplayName("Should save and read multiple message records")
    void shouldSaveAndReadMultipleMessages() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        Conversation conv = createTestConversation(tenantId, user.getId());
        UUID conversationId = conv.getId();
        Message msg1 = Message.create(tenantId, conversationId, "USER", "What is AI?");
        Message msg2 = Message.create(tenantId, conversationId, "ASSISTANT", "AI is artificial intelligence.");
        Message msg3 = Message.create(tenantId, conversationId, "USER", "Can you explain more?");
        messageRepository.saveAll(List.of(msg1, msg2, msg3));

        // When
        List<Message> allMessages = messageRepository.findAll();

        // Then
        assertThat(allMessages).extracting(Message::getContent)
                .contains("What is AI?", "AI is artificial intelligence.", "Can you explain more?");
    }

    @Test
    @DisplayName("Should save messages with different roles")
    void shouldSaveMessagesWithDifferentRoles() {
        // Given
        Tenant tenant = createTestTenant();
        AppUser user = createTestUser(tenant.getId());
        UUID tenantId = tenant.getId();
        Conversation conv = createTestConversation(tenantId, user.getId());
        UUID conversationId = conv.getId();
        Message userMsg = Message.create(tenantId, conversationId, "USER", "User message");
        Message assistantMsg = Message.create(tenantId, conversationId, "ASSISTANT", "Assistant message");
        Message systemMsg = Message.create(tenantId, conversationId, "SYSTEM", "System message");
        messageRepository.saveAll(List.of(userMsg, assistantMsg, systemMsg));

        // When
        List<Message> messages = messageRepository.findByConversationId(conversationId);

        // Then
        assertThat(messages).hasSize(3);
        assertThat(messages).extracting(Message::getRole)
                .containsExactlyInAnyOrder("USER", "ASSISTANT", "SYSTEM");
    }

    @Test
    @DisplayName("Should return empty list when no messages found for conversation")
    void shouldReturnEmptyWhenNoMessagesForConversation() {
        // When
        List<Message> messages = messageRepository.findByConversationId(UUID.randomUUID());

        // Then
        assertThat(messages).isEmpty();
    }
}
