package com.globaltrust.bank.domain;

/**
 * Job roles used for RBAC. Each maps to a Spring authority {@code ROLE_<NAME>}.
 * Note the deliberate separation of duty baked into the role set:
 *  - ADMIN manages users but is NOT allowed to move money.
 *  - APPROVER approves transfers but may never approve one they initiated.
 *  - ADVISOR and TRADER sit on opposite sides of the Chinese Wall.
 */
public enum Role {
    CUSTOMER,
    TELLER,
    APPROVER,
    AUDITOR,
    ADMIN,
    ADVISOR,
    TRADER
}
