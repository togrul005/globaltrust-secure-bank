package com.globaltrust.bank.web;

import com.globaltrust.bank.dto.CompanyDataResponse;
import com.globaltrust.bank.service.AdvisoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advisory")
public class AdvisoryController {

    private final AdvisoryService advisory;

    public AdvisoryController(AdvisoryService advisory) {
        this.advisory = advisory;
    }

    /** Open a client company's file — subject to the Chinese Wall. */
    @GetMapping("/companies/{id}")
    public CompanyDataResponse view(@PathVariable Long id) {
        return advisory.viewCompany(id);
    }
}
