package com.globaltrust.bank.dto;

import com.globaltrust.bank.domain.AuditEvent;
import java.time.Instant;

public record AuditEventResponse(
        Long id, Instant timestamp, String username, String action,
        String resourceType, String resourceId, String details, String outcome
) {
    public static AuditEventResponse of(AuditEvent e) {
        return new AuditEventResponse(e.getId(), e.getTimestamp(), e.getUsername(), e.getAction(),
                e.getResourceType(), e.getResourceId(), e.getDetails(), e.getOutcome());
    }
}
