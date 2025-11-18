package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.ToolIssuanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CSReturnListResponseDto {
    private Long issuanceId;
    private String toolName;
    private String toolImage;      // tool image URL
    private Integer requestedQuantity;        // worker requested in initial request
    private Integer returnedQuantity;
    private String workerName;
    private String workerImage;    // worker profile image URL
    private ToolIssuanceStatus status;
    private LocalDateTime issuedAt;
}
