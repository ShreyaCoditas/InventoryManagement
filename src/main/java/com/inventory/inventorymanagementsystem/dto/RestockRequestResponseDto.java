package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RestockRequestResponseDto {
    private Long id;

    private Long toolId;
    private String toolName;
    private String toolImage;

    private Long factoryId;
    private String factoryName;

    private String requestedBy;

    private Long totalQuantity;
    private Long availableQuantity;
}
