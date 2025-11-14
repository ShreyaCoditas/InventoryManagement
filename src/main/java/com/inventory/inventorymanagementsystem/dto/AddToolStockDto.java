package com.inventory.inventorymanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToolStockDto {
    private Long toolId;
    private Long factoryId;
    private Integer quantity;
}
