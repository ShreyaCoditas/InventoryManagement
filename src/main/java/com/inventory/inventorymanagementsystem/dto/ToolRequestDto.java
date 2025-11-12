package com.inventory.inventorymanagementsystem.dto;

import lombok.*;
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ToolRequestDto {
    private Long toolId;
    private int requestQuantity;
}
