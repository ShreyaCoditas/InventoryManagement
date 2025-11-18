package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CSRequestItemResponseDto {

    private Long requestItemId;
    private Long requestId;

    private String toolName;
    private String toolImage;

    private String workerName;
    private String workerImage;

    private Integer requestedQuantity;
    private Integer availableQuantity;

   // private String storageArea;
    private String tag;   // Expensive / Inexpensive

    private String status; // PENDING, APPROVED, REJECTED, FORWARDED_TO_PH
}

