package com.globaltrust.bank.service;

import com.globaltrust.bank.audit.AuditService;
import com.globaltrust.bank.domain.Account;
import com.globaltrust.bank.dto.AccountResponse;
import com.globaltrust.bank.exception.NotFoundException;
import com.globaltrust.bank.repo.AccountRepository;
import com.globaltrust.bank.security.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accounts;
    private final AuditService audit;

    public AccountService(AccountRepository accounts, AuditService audit) {
        this.accounts = accounts;
        this.audit = audit;
    }

    /** Staff see all accounts; a customer sees only their own (ABAC on ownership). */
    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','APPROVER','AUDITOR')")
    @Transactional(readOnly = true)
    public List<AccountResponse> listVisible() {
        String me = SecurityUtils.currentUsername();
        boolean staff = SecurityUtils.hasRole("TELLER") || SecurityUtils.hasRole("APPROVER")
                || SecurityUtils.hasRole("AUDITOR");
        List<Account> visible = staff ? accounts.findAll() : accounts.findByOwnerUsername(me);
        return visible.stream().map(AccountResponse::of).toList();
    }

    @PreAuthorize("hasAnyRole('CUSTOMER','TELLER','APPROVER','AUDITOR')")
    @Transactional(readOnly = true)
    public AccountResponse get(String accountNumber) {
        Account a = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("No such account: " + accountNumber));
        boolean staff = SecurityUtils.hasRole("TELLER") || SecurityUtils.hasRole("APPROVER")
                || SecurityUtils.hasRole("AUDITOR");
        if (!staff && !a.getOwnerUsername().equals(SecurityUtils.currentUsername())) {
            audit.record("VIEW_ACCOUNT", "ACCOUNT", accountNumber, "cross-account read attempt", "DENIED");
            throw new AccessDeniedException("You may only view your own accounts.");
        }
        return AccountResponse.of(a);
    }
}
