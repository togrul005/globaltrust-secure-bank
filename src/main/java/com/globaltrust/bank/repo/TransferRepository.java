package com.globaltrust.bank.repo;

import com.globaltrust.bank.domain.Transfer;
import com.globaltrust.bank.domain.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findByStatusOrderByCreatedAtDesc(TransferStatus status);
    List<Transfer> findByInitiatedByOrderByCreatedAtDesc(String initiatedBy);
}
