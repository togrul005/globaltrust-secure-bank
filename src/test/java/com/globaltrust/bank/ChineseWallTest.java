package com.globaltrust.bank;

import com.globaltrust.bank.exception.WallViolationException;
import com.globaltrust.bank.service.AdvisoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.*;

/**
 * Proves the Brewer-Nash dynamic wall: opening one company in a CoI class locks
 * the advisor out of competitors in that class, but not out of other classes.
 */
@SpringBootTest
class ChineseWallTest {

    @Autowired
    AdvisoryService advisory;

    @Test
    @WithMockUser(username = "diana", roles = {"ADVISOR"})
    void wall_blocks_competitor_but_allows_other_class() {
        // Acme (id 1, class TECH) -> allowed, locks diana to Acme within TECH
        assertThat(advisory.viewCompany(1L).name()).isEqualTo("Acme Corp");

        // Globex (id 2, class TECH) is a competitor -> blocked
        assertThatThrownBy(() -> advisory.viewCompany(2L))
                .isInstanceOf(WallViolationException.class);

        // Initech (id 3, class PHARMA) is a different class -> still allowed
        assertThat(advisory.viewCompany(3L).name()).isEqualTo("Initech Labs");
    }
}
