package com.globaltrust.bank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A funds transfer and its maker-checker lifecycle. {@code initiatedBy} and
 * {@code approvedBy} are the heart of the Clark-Wilson separation-of-duty rule:
 * they must always be two different people for an above-threshold transfer.
 */
@Entity
@Table(name = "transfer")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromAccount;

    @Column(nullable = false)
    private String toAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;

    @Column(nullable = false)
    private String initiatedBy;

    private String approvedBy;

    private String reason;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant decidedAt;

    public Transfer() { }

    public Transfer(String fromAccount, String toAccount, BigDecimal amount, String initiatedBy, String reason) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.initiatedBy = initiatedBy;
        this.reason = reason;
        this.status = TransferStatus.PENDING_APPROVAL;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getFromAccount() { return fromAccount; }
    public String getToAccount() { return toAccount; }
    public BigDecimal getAmount() { return amount; }
    public TransferStatus getStatus() { return status; }
    public void setStatus(TransferStatus status) { this.status = status; }
    public String getInitiatedBy() { return initiatedBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
