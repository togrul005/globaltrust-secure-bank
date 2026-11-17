package com.globaltrust.bank.repo;

import com.globaltrust.bank.domain.AdvisorWallAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdvisorWallAccessRepository extends JpaRepository<AdvisorWallAccess, Long> {
    Optional<AdvisorWallAccess> findByAdvisorUsernameAndCoiClass(String advisorUsername, String coiClass);
}
