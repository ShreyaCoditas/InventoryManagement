package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkerResponseDto {
    private Long workerId;
    private String name;
    private String email;
    private Long factoryId;
    private String factoryName;
    private Long bayId;
    private String bayName;
    private String status;
}
