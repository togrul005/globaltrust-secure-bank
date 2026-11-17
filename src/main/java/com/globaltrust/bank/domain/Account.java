package com.globaltrust.bank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * A bank account. In Clark-Wilson terms the balance is a Constrained Data Item
 * (CDI): it may only be changed through certified Transformation Procedures
 * (the methods on TransferService), never directly.
 */
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String ownerUsername;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String currency;

    public Account() { }

    public Account(String accountNumber, String ownerUsername, BigDecimal balance, String currency) {
        this.accountNumber = accountNumber;
        this.ownerUsername = ownerUsername;
        this.balance = balance;
        this.currency = currency;
    }

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerUsername() { return ownerUsername; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getCurrency() { return currency; }
}
