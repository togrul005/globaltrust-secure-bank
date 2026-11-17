package com.globaltrust.bank.repo;

import com.globaltrust.bank.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
