package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class OrderStatusUpdateDto {
    private String action;   // ACCEPT or REJECT
    private String reason;   // required only for REJECT
}

