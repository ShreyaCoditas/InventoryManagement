package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRequestItemDetailsDto {
    private Long toolId;
    private String toolName;
    private Integer quantity;
    private String imageUrl;
    private String isExpensive;
}

