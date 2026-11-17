package com.globaltrust.bank.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Append-only audit record. The application never updates or deletes these,
 * satisfying the Clark-Wilson requirement that all TP activity be logged in a
 * tamper-evident way for later inspection by an auditor.
 */
@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;        // the Transformation Procedure name

    private String resourceType;  // e.g. ACCOUNT, TRANSFER, COMPANY
    private String resourceId;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private String outcome;       // GRANTED, DENIED, EXECUTED, ...

    public AuditEvent() { }

    public Long getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
}
