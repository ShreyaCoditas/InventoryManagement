package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToolStockDto {
    @NotNull(message = "Tool ID must not be null")
    private Long toolId;

    @NotNull(message = "Factory ID must not be null")
    private Long factoryId;

    @NotNull(message = "Quantity must not be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}
