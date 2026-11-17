package com.globaltrust.bank.service;

import com.globaltrust.bank.audit.AuditService;
import com.globaltrust.bank.domain.AdvisorWallAccess;
import com.globaltrust.bank.domain.Company;
import com.globaltrust.bank.dto.CompanyDataResponse;
import com.globaltrust.bank.exception.NotFoundException;
import com.globaltrust.bank.exception.WallViolationException;
import com.globaltrust.bank.repo.AdvisorWallAccessRepository;
import com.globaltrust.bank.repo.CompanyRepository;
import com.globaltrust.bank.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Chinese Wall (Brewer-Nash) enforcement for the investment-advisory desk.
 *
 * The first company an advisor opens within a Conflict-of-Interest class is
 * recorded as their "wall". From then on, any attempt to open a competitor in
 * the SAME class is denied, while companies in OTHER classes remain reachable.
 */
@Service
public class AdvisoryService {

    private final CompanyRepository companies;
    private final AdvisorWallAccessRepository walls;
    private final AuditService audit;

    public AdvisoryService(CompanyRepository companies,
                           AdvisorWallAccessRepository walls,
                           AuditService audit) {
        this.companies = companies;
        this.walls = walls;
        this.audit = audit;
    }

    @PreAuthorize("hasRole('ADVISOR')")
    @Transactional
    public CompanyDataResponse viewCompany(Long companyId) {
        String advisor = SecurityUtils.currentUsername();
        Company c = companies.findById(companyId)
                .orElseThrow(() -> new NotFoundException("No such company: " + companyId));

        Optional<AdvisorWallAccess> existing =
                walls.findByAdvisorUsernameAndCoiClass(advisor, c.getCoiClass());

        if (existing.isPresent() && !existing.get().getCompanyId().equals(c.getId())) {
            audit.record("VIEW_COMPANY", "COMPANY", String.valueOf(companyId),
                    "Chinese Wall block: already engaged with company " + existing.get().getCompanyId()
                            + " in CoI class " + c.getCoiClass(), "DENIED");
            throw new WallViolationException(
                    "Chinese Wall: you are already engaged with a competitor in CoI class '"
                            + c.getCoiClass() + "' and cannot access " + c.getName() + ".");
        }

        if (existing.isEmpty()) {
            walls.save(new AdvisorWallAccess(advisor, c.getCoiClass(), c.getId()));
        }
        audit.record("VIEW_COMPANY", "COMPANY", String.valueOf(companyId),
                "access to " + c.getName() + " (CoI class " + c.getCoiClass() + ")", "GRANTED");

        return new CompanyDataResponse(c.getId(), c.getName(), c.getCoiClass(), c.getMnpiSummary(),
                "Access granted. You are now walled to '" + c.getName()
                        + "' within CoI class '" + c.getCoiClass() + "'.");
    }
}
