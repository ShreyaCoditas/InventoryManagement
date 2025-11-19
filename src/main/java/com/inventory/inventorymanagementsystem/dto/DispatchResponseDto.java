package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

import java.util.List;

@Data
public class DispatchResponseDto {
    private Long orderId;
    private String orderCode;
    private String status;
    private Integer requestedQuantity;
    private Integer totalSent;
    private Integer remaining;
    private String paymentStatus; // PAID / UNPAID

    private InvoiceSimpleDto invoice;
}

