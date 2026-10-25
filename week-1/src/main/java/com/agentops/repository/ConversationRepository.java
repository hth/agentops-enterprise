package com.agentops.repository;

import com.agentops.domain.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByTenantId(UUID tenantId);
    List<Conversation> findByUserId(UUID userId);
    List<Conversation> findByTenantIdAndUserId(UUID tenantId, UUID userId);
}
