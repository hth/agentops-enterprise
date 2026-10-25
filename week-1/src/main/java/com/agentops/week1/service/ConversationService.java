package com.agentops.week1.service;

import com.agentops.week1.domain.conversation.Conversation;
import com.agentops.week1.repository.ConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final CurrentTenantService currentTenantService;

    public ConversationService(ConversationRepository conversationRepository, CurrentTenantService currentTenantService) {
        this.conversationRepository = conversationRepository;
        this.currentTenantService = currentTenantService;
    }

    public Optional<Conversation> findById(UUID id) {
        return conversationRepository.findById(id);
    }

    public List<Conversation> findByTenantId(UUID tenantId) {
        return conversationRepository.findByTenantId(tenantId);
    }

    public List<Conversation> findByUserId(UUID userId) {
        return conversationRepository.findByUserId(userId);
    }

    public List<Conversation> findByTenantIdAndUserId(UUID tenantId, UUID userId) {
        return conversationRepository.findByTenantIdAndUserId(tenantId, userId);
    }

    public Conversation create(UUID tenantId, UUID userId, String title) {
        return conversationRepository.save(Conversation.create(tenantId, userId, title));
    }

    public void delete(UUID id) {
        conversationRepository.deleteById(id);
    }
}
