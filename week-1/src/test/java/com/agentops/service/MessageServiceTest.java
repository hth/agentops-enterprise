package com.agentops.service;

import com.agentops.domain.conversation.Message;
import com.agentops.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private UUID tenantId;
    private UUID conversationId;
    private UUID messageId;
    private Message message;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        messageId = UUID.randomUUID();
        message = Message.create(tenantId, conversationId, "USER", "Hello World");
    }

    @Test
    @DisplayName("Should find message by ID when exists")
    void shouldFindByIdWhenExists() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        Optional<Message> result = messageService.findById(messageId);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(message);
        verify(messageRepository, times(1)).findById(messageId);
    }

    @Test
    @DisplayName("Should return empty optional when message ID does not exist")
    void shouldReturnEmptyWhenNotFound() {
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        Optional<Message> result = messageService.findById(messageId);

        assertThat(result).isEmpty();
        verify(messageRepository, times(1)).findById(messageId);
    }

    @Test
    @DisplayName("Should find messages by conversation ID")
    void shouldFindByConversationId() {
        Message msg2 = Message.create(tenantId, conversationId, "ASSISTANT", "Hi there!");
        when(messageRepository.findByConversationId(conversationId)).thenReturn(List.of(message, msg2));

        List<Message> results = messageService.findByConversationId(conversationId);

        assertThat(results).hasSize(2).containsExactly(message, msg2);
        verify(messageRepository, times(1)).findByConversationId(conversationId);
    }

    @Test
    @DisplayName("Should find messages by tenant ID")
    void shouldFindByTenantId() {
        when(messageRepository.findByTenantId(tenantId)).thenReturn(List.of(message));

        List<Message> results = messageService.findByTenantId(tenantId);

        assertThat(results).hasSize(1).containsExactly(message);
        verify(messageRepository, times(1)).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should find messages by tenant ID and conversation ID")
    void shouldFindByTenantIdAndConversationId() {
        when(messageRepository.findByTenantIdAndConversationId(tenantId, conversationId)).thenReturn(List.of(message));

        List<Message> results = messageService.findByTenantIdAndConversationId(tenantId, conversationId);

        assertThat(results).hasSize(1).containsExactly(message);
        verify(messageRepository, times(1)).findByTenantIdAndConversationId(tenantId, conversationId);
    }

    @Test
    @DisplayName("Should create and save a new message")
    void shouldCreateAndSaveMessage() {
        String role = "USER";
        String content = "How do I test this?";
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message created = messageService.create(tenantId, conversationId, role, content);

        assertThat(created).isNotNull();
        assertThat(created.getTenantId()).isEqualTo(tenantId);
        assertThat(created.getConversationId()).isEqualTo(conversationId);
        assertThat(created.getRole()).isEqualTo(role);
        assertThat(created.getContent()).isEqualTo(content);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, times(1)).save(captor.capture());
        Message captured = captor.getValue();
        assertThat(captured.getTenantId()).isEqualTo(tenantId);
        assertThat(captured.getConversationId()).isEqualTo(conversationId);
        assertThat(captured.getRole()).isEqualTo(role);
        assertThat(captured.getContent()).isEqualTo(content);
    }

    @Test
    @DisplayName("Should delete message by ID")
    void shouldDeleteMessageById() {
        doNothing().when(messageRepository).deleteById(messageId);

        messageService.delete(messageId);

        verify(messageRepository, times(1)).deleteById(messageId);
    }
}
