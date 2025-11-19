package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CSReturnVerificationDto {
    @NotNull(message = "Issuance ID must not be null")
    private Long issuanceId;

    @NotNull(message = "Fit quantity must not be null")
    @Min(value = 0, message = "Fit quantity cannot be negative")
    private Integer fitQuantity;

    @NotNull(message = "Unfit quantity must not be null")
    @Min(value = 0, message = "Unfit quantity cannot be negative")
    private Integer unfitQuantity;
    // damaged (will reduce total)
}
