package com.globaltrust.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateTransferRequest(
        @NotBlank String fromAccount,
        @NotBlank String toAccount,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String reason
) { }
