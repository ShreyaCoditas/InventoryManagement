package com.inventory.inventorymanagementsystem.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ToolRequestResponseDto {
    private Long requestId;
    private String toolName;
    private String imageUrl;
    private int requestedQuantity;
    private String isPerishable;
    private String isExpensive;
    private String status; // Pending, Assigned, Overdue, Seized
    private LocalDateTime requestDate;
    private LocalDateTime assignedDate;
    private LocalDateTime dueDate;
}
