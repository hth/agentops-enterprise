package com.agentops.week1.service;

import com.agentops.week1.domain.conversation.Conversation;
import com.agentops.week1.repository.ConversationRepository;
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
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private CurrentTenantService currentTenantService;

    @InjectMocks
    private ConversationService conversationService;

    private UUID tenantId;
    private UUID userId;
    private UUID conversationId;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        conversationId = UUID.randomUUID();
        conversation = Conversation.create(tenantId, userId, "Test Conversation");
    }

    @Test
    @DisplayName("Should find conversation by ID when exists")
    void shouldFindByIdWhenExists() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.of(conversation));

        Optional<Conversation> result = conversationService.findById(conversationId);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(conversation);
        verify(conversationRepository, times(1)).findById(conversationId);
    }

    @Test
    @DisplayName("Should return empty optional when conversation ID does not exist")
    void shouldReturnEmptyWhenNotFound() {
        when(conversationRepository.findById(conversationId)).thenReturn(Optional.empty());

        Optional<Conversation> result = conversationService.findById(conversationId);

        assertThat(result).isEmpty();
        verify(conversationRepository, times(1)).findById(conversationId);
    }

    @Test
    @DisplayName("Should find conversations by tenant ID")
    void shouldFindByTenantId() {
        Conversation conv2 = Conversation.create(tenantId, userId, "Second Conversation");
        when(conversationRepository.findByTenantId(tenantId)).thenReturn(List.of(conversation, conv2));

        List<Conversation> results = conversationService.findByTenantId(tenantId);

        assertThat(results).hasSize(2).containsExactly(conversation, conv2);
        verify(conversationRepository, times(1)).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should find conversations by user ID")
    void shouldFindByUserId() {
        when(conversationRepository.findByUserId(userId)).thenReturn(List.of(conversation));

        List<Conversation> results = conversationService.findByUserId(userId);

        assertThat(results).hasSize(1).containsExactly(conversation);
        verify(conversationRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("Should find conversations by tenant ID and user ID")
    void shouldFindByTenantIdAndUserId() {
        when(conversationRepository.findByTenantIdAndUserId(tenantId, userId)).thenReturn(List.of(conversation));

        List<Conversation> results = conversationService.findByTenantIdAndUserId(tenantId, userId);

        assertThat(results).hasSize(1).containsExactly(conversation);
        verify(conversationRepository, times(1)).findByTenantIdAndUserId(tenantId, userId);
    }

    @Test
    @DisplayName("Should create and save a new conversation")
    void shouldCreateAndSaveConversation() {
        String title = "New Chat";
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Conversation created = conversationService.create(tenantId, userId, title);

        assertThat(created).isNotNull();
        assertThat(created.getTenantId()).isEqualTo(tenantId);
        assertThat(created.getUserId()).isEqualTo(userId);
        assertThat(created.getTitle()).isEqualTo(title);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository, times(1)).save(captor.capture());
        Conversation captured = captor.getValue();
        assertThat(captured.getTenantId()).isEqualTo(tenantId);
        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getTitle()).isEqualTo(title);
    }

    @Test
    @DisplayName("Should delete conversation by ID")
    void shouldDeleteConversationById() {
        doNothing().when(conversationRepository).deleteById(conversationId);

        conversationService.delete(conversationId);

        verify(conversationRepository, times(1)).deleteById(conversationId);
    }
}
