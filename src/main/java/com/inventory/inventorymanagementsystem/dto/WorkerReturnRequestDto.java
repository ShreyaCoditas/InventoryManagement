package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class WorkerReturnRequestDto {
    private Long issuanceId;
    private Integer quantity; // how many items worker is returning (<= issuance.quantity)
}
