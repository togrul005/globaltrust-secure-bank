package com.globaltrust.bank.domain;

import jakarta.persistence.*;

/**
 * A client company advised by the investment arm. {@code coiClass} groups
 * competitors into one Conflict-of-Interest class for the Chinese Wall policy:
 * two companies sharing a coiClass are competitors.
 */
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String coiClass;

    @Column(length = 1000)
    private String mnpiSummary; // material non-public information (demo placeholder)

    public Company() { }

    public Company(String name, String coiClass, String mnpiSummary) {
        this.name = name;
        this.coiClass = coiClass;
        this.mnpiSummary = mnpiSummary;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCoiClass() { return coiClass; }
    public String getMnpiSummary() { return mnpiSummary; }
}
