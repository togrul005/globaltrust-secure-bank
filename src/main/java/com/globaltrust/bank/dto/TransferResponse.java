package com.globaltrust.bank.dto;

import com.globaltrust.bank.domain.Transfer;
import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(
        Long id,
        String fromAccount,
        String toAccount,
        BigDecimal amount,
        String status,
        String initiatedBy,
        String approvedBy,
        String reason,
        Instant createdAt,
        Instant decidedAt
) {
    public static TransferResponse of(Transfer t) {
        return new TransferResponse(
                t.getId(), t.getFromAccount(), t.getToAccount(), t.getAmount(),
                t.getStatus().name(), t.getInitiatedBy(), t.getApprovedBy(),
                t.getReason(), t.getCreatedAt(), t.getDecidedAt());
    }
}
