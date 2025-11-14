package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRequestDto {
    private List<Long> toolIds;   // Multiple tools in one request
    private Integer quantity;     // Optional common quantity
    private String comment;
}
