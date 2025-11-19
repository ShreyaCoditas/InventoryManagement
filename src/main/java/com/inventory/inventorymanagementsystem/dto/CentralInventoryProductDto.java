package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class CentralInventoryProductDto {

    private Long productId;
    private String name;
    private String image;
    private String categoryName;
    private Double price;

    private Long availableQuantity;


}
