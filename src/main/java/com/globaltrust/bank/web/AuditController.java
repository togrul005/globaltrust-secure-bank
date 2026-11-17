package com.globaltrust.bank.web;

import com.globaltrust.bank.dto.AuditEventResponse;
import com.globaltrust.bank.repo.AuditEventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEventRepository audit;

    public AuditController(AuditEventRepository audit) {
        this.audit = audit;
    }

    /** Read-only access to the audit trail for auditors and admins. */
    @PreAuthorize("hasAnyRole('AUDITOR','ADMIN')")
    @GetMapping
    public List<AuditEventResponse> recent() {
        return audit.findTop200ByOrderByTimestampDesc()
                .stream().map(AuditEventResponse::of).toList();
    }
}
