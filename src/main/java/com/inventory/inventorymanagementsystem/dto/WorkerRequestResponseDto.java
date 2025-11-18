package com.inventory.inventorymanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkerRequestResponseDto {
    private Long itemId;
    private String toolName;
    private String toolImage;
    private Integer requestedQuantity;
    private String requestedDate;
    private String returnWindow;     // 30 days etc.
    private String status;           // Pending / Rejected / Allocated
    private String rejectionReason;
}
