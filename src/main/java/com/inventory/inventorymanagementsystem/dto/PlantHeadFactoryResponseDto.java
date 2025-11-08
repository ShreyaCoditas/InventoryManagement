package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantHeadFactoryResponseDto {
    private Long factoryId;
    private String factoryName;
    private String factoryLocation;
}

