// ToolRequestItemDetailsDto.java
package com.inventory.inventorymanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ToolRequestItemDetailsDto {
    private Long toolId;
    private String toolName;
    private Integer quantity;
    private Boolean isExpensive;
    private String categoryName;
}