package com.globaltrust.bank.dto;

import com.globaltrust.bank.domain.Account;
import java.math.BigDecimal;

public record AccountResponse(String accountNumber, String owner, BigDecimal balance, String currency) {
    public static AccountResponse of(Account a) {
        return new AccountResponse(a.getAccountNumber(), a.getOwnerUsername(), a.getBalance(), a.getCurrency());
    }
}
