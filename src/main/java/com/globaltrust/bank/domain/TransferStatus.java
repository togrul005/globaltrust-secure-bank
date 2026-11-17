package com.globaltrust.bank.domain;

public enum TransferStatus {
    /** Created by a maker, waiting for a different person to approve. */
    PENDING_APPROVAL,
    /** Funds moved and committed. */
    EXECUTED,
    /** Declined by an approver (or failed an integrity check at enforcement time). */
    REJECTED
}
