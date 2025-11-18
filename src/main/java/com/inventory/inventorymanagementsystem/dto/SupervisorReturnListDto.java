package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SupervisorReturnListDto {
    private Long returnId;
    private Long issuanceId;

    private String toolName;
    private String workerName;
    private Integer requestedQuantity;
    private Integer pendingQuantity;

    private String status;        // PENDING_RETURN / REQUESTED_RETURN / OVERDUE / SEIZED
    private String returnDueDate; // formatted string
    private boolean overdue;
}
