package com.agentops.service;

import com.agentops.domain.conversation.Message;
import com.agentops.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Optional<Message> findById(UUID id) {
        return messageRepository.findById(id);
    }

    public List<Message> findByConversationId(UUID conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }

    public List<Message> findByTenantId(UUID tenantId) {
        return messageRepository.findByTenantId(tenantId);
    }

    public List<Message> findByTenantIdAndConversationId(UUID tenantId, UUID conversationId) {
        return messageRepository.findByTenantIdAndConversationId(tenantId, conversationId);
    }

    public Message create(UUID tenantId, UUID conversationId, String role, String content) {
        return messageRepository.save(Message.create(tenantId, conversationId, role, content));
    }

    public void delete(UUID id) {
        messageRepository.deleteById(id);
    }
}
