package com.globaltrust.bank;

import com.globaltrust.bank.domain.TransferStatus;
import com.globaltrust.bank.dto.CreateTransferRequest;
import com.globaltrust.bank.dto.TransferResponse;
import com.globaltrust.bank.exception.SoDViolationException;
import com.globaltrust.bank.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Proves the Clark-Wilson separation-of-duty rule: an above-threshold transfer
 * created by one person cannot be approved by that same person, but can be
 * approved by a different approver.
 */
@SpringBootTest
class SeparationOfDutyTest {

    @Autowired
    TransferService transfers;

    @Test
    @WithMockUser(username = "victor", roles = {"APPROVER"})
    void maker_cannot_approve_their_own_transfer() {
        TransferResponse t = transfers.createTransfer(
                new CreateTransferRequest("ACC-9000", "ACC-1001", new BigDecimal("2500.00"), "ops top-up"));
        assertThat(t.status()).isEqualTo(TransferStatus.PENDING_APPROVAL.name());

        // victor (the maker) tries to approve their own transfer -> blocked
        assertThatThrownBy(() -> transfers.approveTransfer(t.id()))
                .isInstanceOf(SoDViolationException.class);
    }

    @Test
    void second_approver_can_execute_makers_transfer() {
        // maker = victor
        TransferResponse pending = runAs("victor", "APPROVER",
                () -> transfers.createTransfer(
                        new CreateTransferRequest("ACC-9000", "ACC-1002", new BigDecimal("3000.00"), "ops")));
        assertThat(pending.status()).isEqualTo(TransferStatus.PENDING_APPROVAL.name());

        // checker = wendy (a different approver) -> executes
        TransferResponse executed = runAs("wendy", "APPROVER",
                () -> transfers.approveTransfer(pending.id()));
        assertThat(executed.status()).isEqualTo(TransferStatus.EXECUTED.name());
        assertThat(executed.approvedBy()).isEqualTo("wendy");
        assertThat(executed.initiatedBy()).isEqualTo("victor");
    }

    /** Run a unit of work under a given mock user/role within the security context. */
    private <T> T runAs(String user, String role, java.util.function.Supplier<T> work) {
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                user, "n/a",
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role)));
        var ctx = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(ctx);
        try {
            return work.get();
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
