package com.inventory.inventorymanagementsystem.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInventoryResponseDto {

    private Long id;
    private String name;
    private String productDescription;
    private BigDecimal price;
    private Integer rewardPoint;
    private String categoryName;
    private String image;
    private String isActive;
    private Integer totalQuantity;
    private String stockStatus;
}

