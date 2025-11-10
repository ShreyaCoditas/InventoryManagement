package com.inventory.inventorymanagementsystem.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkerListResponseDto {
    private Long workerId;
    private String workerName;
    private String factoryName;
    private String location;
    private String bayArea;
    private String status;
}

