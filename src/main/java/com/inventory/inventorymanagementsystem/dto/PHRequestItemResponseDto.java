package com.inventory.inventorymanagementsystem.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class PHRequestItemResponseDto {
    private Long itemId;
    private Long requestId;

    private String toolName;
    private String toolImage;

    private String workerName;
    private String workerImage;

    private Integer requestedQuantity;
    private Integer availableQuantity;

    private String tag;     // Expensive
    private String status;  // FORWARDED_TO_PH / APPROVED / REJECTED
}
