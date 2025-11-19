package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class DistributorPlaceOrderResponseDto {
    private Long orderId;
    private String orderCode;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;
    private String status;
}
