package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRequestResponseDto {
    private Long requestId;
    private Long workerId;
    private String workerName;
    private String workerBay;
    private String status;
    private String rejectionReason;
    private LocalDateTime createdAt;

    private List<ToolRequestItemDetailsDto> items;
}
