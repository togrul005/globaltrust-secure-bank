package com.globaltrust.bank.repo;

import com.globaltrust.bank.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    List<AuditEvent> findTop200ByOrderByTimestampDesc();
}
