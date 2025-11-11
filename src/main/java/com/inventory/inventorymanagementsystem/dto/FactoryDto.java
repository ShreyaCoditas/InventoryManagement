package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class FactoryDto {

    private Long factoryId;
    private String factoryName;
    private String location;
    private String plantHeadName;
    private int totalProducts;
    private int totalWorkers;
    private int totalTools;
    private String status;
    private String chiefSupervisorName;  // ✅ New field

    // Optional: If you want to keep an explicit constructor for clarity
    public FactoryDto(Long factoryId, String factoryName, String location, String plantHeadName,
                      int totalProducts, int totalWorkers, int totalTools, String status,
                      String chiefSupervisorName) {
        this.factoryId = factoryId;
        this.factoryName = factoryName;
        this.location = location;
        this.plantHeadName = plantHeadName;
        this.totalProducts = totalProducts;
        this.totalWorkers = totalWorkers;
        this.totalTools = totalTools;
        this.status = status;
        this.chiefSupervisorName = chiefSupervisorName;
    }
}
