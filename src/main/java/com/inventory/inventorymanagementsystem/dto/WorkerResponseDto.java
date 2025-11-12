package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WorkerResponseDto {
    private Long workerId;
    private String workerName;
    private String factoryName;
    private Long factoryId;        // Added
    private String location;
    private String bayArea;
    private String status;        // ACTIVE / INACTIVE
}