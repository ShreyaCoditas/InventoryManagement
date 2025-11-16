package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ToolRequestItemResponseDto {
    private Long itemId;
    private Long requestId;
    private String workerName;
    private Long workerId;
    private String toolName;
    private Long toolId;
    private Integer quantity;
    private String toolType; // NORMAL or EXPENSIVE
    private String status;   // PENDING / APPROVED / REJECTED / SENT_TO_PH
}

