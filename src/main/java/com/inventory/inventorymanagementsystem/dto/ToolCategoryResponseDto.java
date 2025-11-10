package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolCategoryResponseDto {
    private Long id;
    private String categoryName;
    private String categoryDescription;
}
