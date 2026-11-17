package com.globaltrust.bank.domain;

import jakarta.persistence.*;

/**
 * Records, per advisor and per Conflict-of-Interest class, the single company
 * that advisor first engaged with. This is the "wall" in Brewer-Nash: once a
 * row exists for (advisor, coiClass), the advisor is locked to that company and
 * barred from any competitor in the same class.
 */
@Entity
@Table(name = "advisor_wall_access",
       uniqueConstraints = @UniqueConstraint(columnNames = {"advisorUsername", "coiClass"}))
public class AdvisorWallAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String advisorUsername;

    @Column(nullable = false)
    private String coiClass;

    @Column(nullable = false)
    private Long companyId;

    public AdvisorWallAccess() { }

    public AdvisorWallAccess(String advisorUsername, String coiClass, Long companyId) {
        this.advisorUsername = advisorUsername;
        this.coiClass = coiClass;
        this.companyId = companyId;
    }

    public Long getId() { return id; }
    public String getAdvisorUsername() { return advisorUsername; }
    public String getCoiClass() { return coiClass; }
    public Long getCompanyId() { return companyId; }
}
