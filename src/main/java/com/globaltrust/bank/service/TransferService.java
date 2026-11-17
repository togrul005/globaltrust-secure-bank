package com.globaltrust.bank.service;

import com.globaltrust.bank.audit.AuditService;
import com.globaltrust.bank.domain.Account;
import com.globaltrust.bank.domain.Transfer;
import com.globaltrust.bank.domain.TransferStatus;
import com.globaltrust.bank.dto.CreateTransferRequest;
import com.globaltrust.bank.dto.TransferResponse;
import com.globaltrust.bank.exception.*;
import com.globaltrust.bank.repo.AccountRepository;
import com.globaltrust.bank.repo.TransferRepository;
import com.globaltrust.bank.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * The Clark-Wilson Transformation Procedures (TPs) for moving money.
 *
 *  - Account balances are Constrained Data Items (CDIs); they change ONLY here.
 *  - Every TP runs integrity checks (the IVP role) before it commits.
 *  - createTransfer / approveTransfer enforce SEPARATION OF DUTY: an
 *    above-threshold transfer needs two different people (maker and checker).
 *  - Every TP writes an audit record.
 */
@Service
public class TransferService {

    private final AccountRepository accounts;
    private final TransferRepository transfers;
    private final AuditService audit;
    private final BigDecimal autoApproveThreshold;

    public TransferService(AccountRepository accounts,
                           TransferRepository transfers,
                           AuditService audit,
                           @Value("${bank.auto-approve-threshold}") BigDecimal autoApproveThreshold) {
        this.accounts = accounts;
        this.transfers = transfers;
        this.audit = audit;
        this.autoApproveThreshold = autoApproveThreshold;
    }

    /** MAKER step. Validates the request, then either auto-executes (small) or parks it for approval (large). */
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','APPROVER')")
    @Transactional
    public TransferResponse createTransfer(CreateTransferRequest req) {
        String me = SecurityUtils.currentUsername();

        Account from = accounts.findByAccountNumber(req.fromAccount())
                .orElseThrow(() -> new NotFoundException("No such source account: " + req.fromAccount()));
        Account to = accounts.findByAccountNumber(req.toAccount())
                .orElseThrow(() -> new NotFoundException("No such destination account: " + req.toAccount()));

        // --- well-formedness / IVP checks ---
        if (from.getId().equals(to.getId())) {
            throw new BadRequestException("Source and destination accounts must differ.");
        }
        if (req.amount().signum() <= 0) {
            throw new BadRequestException("Amount must be positive.");
        }
        // ABAC: a plain customer may only spend from an account they own.
        boolean staff = SecurityUtils.hasRole("TELLER") || SecurityUtils.hasRole("APPROVER");
        if (!staff && !from.getOwnerUsername().equals(me)) {
            audit.record("CREATE_TRANSFER", "ACCOUNT", from.getAccountNumber(),
                    "customer attempted to debit an account they do not own", "DENIED");
            throw new AccessDeniedException("You may only transfer from your own account.");
        }
        if (from.getBalance().compareTo(req.amount()) < 0) {
            throw new BadRequestException("Insufficient funds.");
        }

        Transfer t = new Transfer(from.getAccountNumber(), to.getAccountNumber(),
                req.amount(), me, req.reason());

        if (req.amount().compareTo(autoApproveThreshold) <= 0) {
            // Single-control TP: small enough to execute immediately.
            applyFunds(from, to, req.amount());
            t.setStatus(TransferStatus.EXECUTED);
            t.setApprovedBy("AUTO");
            t.setDecidedAt(Instant.now());
            transfers.save(t);
            audit.record("CREATE_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                    "auto-executed (<= threshold) " + req.amount(), "EXECUTED");
        } else {
            // Above threshold: requires a second person. Park as PENDING_APPROVAL.
            transfers.save(t);
            audit.record("CREATE_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                    "queued for approval, amount " + req.amount() + ", maker " + me, "PENDING");
        }
        return TransferResponse.of(t);
    }

    /** CHECKER step. Enforces separation of duty, re-checks integrity, then commits. */
    @PreAuthorize("hasRole('APPROVER')")
    @Transactional
    public TransferResponse approveTransfer(Long transferId) {
        String me = SecurityUtils.currentUsername();
        Transfer t = transfers.findById(transferId)
                .orElseThrow(() -> new NotFoundException("No such transfer: " + transferId));

        if (t.getStatus() != TransferStatus.PENDING_APPROVAL) {
            throw new ConflictException("Transfer is not awaiting approval (status=" + t.getStatus() + ").");
        }
        // *** SEPARATION OF DUTY *** the maker can never be the checker.
        if (t.getInitiatedBy().equals(me)) {
            audit.record("APPROVE_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                    "maker attempted to approve their own transfer", "DENIED");
            throw new SoDViolationException(
                    "Separation of duty: the initiator (" + me + ") cannot approve their own transfer.");
        }

        Account from = accounts.findByAccountNumber(t.getFromAccount())
                .orElseThrow(() -> new NotFoundException("Source account vanished: " + t.getFromAccount()));
        Account to = accounts.findByAccountNumber(t.getToAccount())
                .orElseThrow(() -> new NotFoundException("Destination account vanished: " + t.getToAccount()));

        // IVP re-check at enforcement time: funds may have changed since creation.
        if (from.getBalance().compareTo(t.getAmount()) < 0) {
            t.setStatus(TransferStatus.REJECTED);
            t.setApprovedBy(me);
            t.setDecidedAt(Instant.now());
            transfers.save(t);
            audit.record("APPROVE_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                    "rejected at approval: insufficient funds", "REJECTED");
            throw new BadRequestException("Insufficient funds at approval time; transfer rejected.");
        }

        applyFunds(from, to, t.getAmount());
        t.setStatus(TransferStatus.EXECUTED);
        t.setApprovedBy(me);
        t.setDecidedAt(Instant.now());
        transfers.save(t);
        audit.record("APPROVE_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                "approved by " + me + " (maker " + t.getInitiatedBy() + "), amount " + t.getAmount(), "EXECUTED");
        return TransferResponse.of(t);
    }

    /** CHECKER step (decline). Also subject to separation of duty. */
    @PreAuthorize("hasRole('APPROVER')")
    @Transactional
    public TransferResponse rejectTransfer(Long transferId, String reason) {
        String me = SecurityUtils.currentUsername();
        Transfer t = transfers.findById(transferId)
                .orElseThrow(() -> new NotFoundException("No such transfer: " + transferId));

        if (t.getStatus() != TransferStatus.PENDING_APPROVAL) {
            throw new ConflictException("Transfer is not awaiting approval (status=" + t.getStatus() + ").");
        }
        if (t.getInitiatedBy().equals(me)) {
            audit.record("REJECT_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                    "maker attempted to decide their own transfer", "DENIED");
            throw new SoDViolationException(
                    "Separation of duty: the initiator (" + me + ") cannot decide their own transfer.");
        }
        t.setStatus(TransferStatus.REJECTED);
        t.setApprovedBy(me);
        t.setDecidedAt(Instant.now());
        transfers.save(t);
        audit.record("REJECT_TRANSFER", "TRANSFER", String.valueOf(t.getId()),
                "rejected by " + me + ": " + (reason == null ? "" : reason), "REJECTED");
        return TransferResponse.of(t);
    }

    @PreAuthorize("hasAnyRole('APPROVER','AUDITOR')")
    @Transactional(readOnly = true)
    public List<TransferResponse> listPending() {
        return transfers.findByStatusOrderByCreatedAtDesc(TransferStatus.PENDING_APPROVAL)
                .stream().map(TransferResponse::of).toList();
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','APPROVER','AUDITOR')")
    @Transactional(readOnly = true)
    public List<TransferResponse> listMine() {
        return transfers.findByInitiatedByOrderByCreatedAtDesc(SecurityUtils.currentUsername())
                .stream().map(TransferResponse::of).toList();
    }

    /** The only place a CDI (balance) is mutated. */
    private void applyFunds(Account from, Account to, BigDecimal amount) {
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accounts.save(from);
        accounts.save(to);
    }
}
