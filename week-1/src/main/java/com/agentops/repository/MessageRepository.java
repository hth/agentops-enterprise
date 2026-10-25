package com.agentops.repository;

import com.agentops.domain.conversation.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationId(UUID conversationId);
    List<Message> findByTenantId(UUID tenantId);
    List<Message> findByTenantIdAndConversationId(UUID tenantId, UUID conversationId);
}
