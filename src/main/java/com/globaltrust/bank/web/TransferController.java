package com.globaltrust.bank.web;

import com.globaltrust.bank.dto.CreateTransferRequest;
import com.globaltrust.bank.dto.DecisionRequest;
import com.globaltrust.bank.dto.TransferResponse;
import com.globaltrust.bank.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transfers;

    public TransferController(TransferService transfers) {
        this.transfers = transfers;
    }

    /** Maker creates a transfer. */
    @PostMapping
    public TransferResponse create(@Valid @RequestBody CreateTransferRequest req) {
        return transfers.createTransfer(req);
    }

    /** Checker approves (executes) a pending transfer. */
    @PostMapping("/{id}/approve")
    public TransferResponse approve(@PathVariable Long id) {
        return transfers.approveTransfer(id);
    }

    /** Checker rejects a pending transfer. */
    @PostMapping("/{id}/reject")
    public TransferResponse reject(@PathVariable Long id, @RequestBody(required = false) DecisionRequest body) {
        return transfers.rejectTransfer(id, body == null ? null : body.reason());
    }

    @GetMapping("/pending")
    public List<TransferResponse> pending() {
        return transfers.listPending();
    }

    @GetMapping("/mine")
    public List<TransferResponse> mine() {
        return transfers.listMine();
    }
}
