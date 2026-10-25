package com.agentops.dto;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        UUID tenantId,
        String email,
        String role,
        String status
) {
}
