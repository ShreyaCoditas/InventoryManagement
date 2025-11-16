package com.inventory.inventorymanagementsystem.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ToolResponseDto {
    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private Long categoryId;
    private String imageUrl;
    private String isPerishable;
    private String isExpensive;
    private Integer threshold;
    private Integer availableQuantity;
    private Long totalQuantity;
    private String status;
    private String stockStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ADD THIS LINE ONLY
    private Integer returnWindowDays = 30;
}
