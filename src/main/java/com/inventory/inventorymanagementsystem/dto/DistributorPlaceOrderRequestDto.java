package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class DistributorPlaceOrderRequestDto {
    @NotNull(message = "Product ID is needed")
    private Long productId;
    @NotNull(message = "Quantity is required")
    @Min(value=1,message = "quantity to be more than 1")
    private Integer quantity;
}

