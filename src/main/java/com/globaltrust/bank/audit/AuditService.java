package com.globaltrust.bank.audit;

import com.globaltrust.bank.domain.AuditEvent;
import com.globaltrust.bank.repo.AuditEventRepository;
import com.globaltrust.bank.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Writes append-only audit records. Implements the Clark-Wilson requirement
 * that every Transformation Procedure logs enough to reconstruct what happened.
 * Runs in its own transaction so a denied/rolled-back action is still recorded.
 */
@Service
public class AuditService {

    private final AuditEventRepository repo;

    public AuditService(AuditEventRepository repo) {
        this.repo = repo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String resourceType, String resourceId, String details, String outcome) {
        AuditEvent e = new AuditEvent();
        e.setTimestamp(Instant.now());
        e.setUsername(SecurityUtils.currentUsername());
        e.setAction(action);
        e.setResourceType(resourceType);
        e.setResourceId(resourceId);
        e.setDetails(details);
        e.setOutcome(outcome);
        repo.save(e);
    }
}
