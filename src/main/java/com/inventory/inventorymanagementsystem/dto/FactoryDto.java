package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryDto {
    private Long factoryId;
    private String factoryName;
    private String location;
    private String plantHeadName;
    private int totalProducts;
    private int totalWorkers;  // ✅ New
    private int totalTools;    // ✅ New
    private String status;
}
