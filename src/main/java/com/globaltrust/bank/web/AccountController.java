package com.globaltrust.bank.web;

import com.globaltrust.bank.dto.AccountResponse;
import com.globaltrust.bank.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accounts;

    public AccountController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public List<AccountResponse> list() {
        return accounts.listVisible();
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse get(@PathVariable String accountNumber) {
        return accounts.get(accountNumber);
    }
}
