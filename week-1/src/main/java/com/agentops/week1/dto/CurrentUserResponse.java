package com.agentops.week1.dto;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        UUID tenantId,
        String email,
        String role,
        String status
) {
}
