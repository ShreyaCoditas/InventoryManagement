package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class CSReturnVerificationDto {
    private Long issuanceId;
    private Integer fitQuantity;   // fit quantity returned (will be added to available)
    private Integer unfitQuantity; // damaged (will reduce total)
}
