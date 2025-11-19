package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class OrderBatchDto {
    private String batchReference;
    private Integer quantity;
    private String sentAt;
}
