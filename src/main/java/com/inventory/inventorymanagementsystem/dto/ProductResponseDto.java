package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String name;
    private String productDescription;
    private BigDecimal price;
    private Integer rewardPoint;
    private String categoryName;
    private String image;
    private String isActive;
//    private Integer totalQuantity;
//    private String stockStatus;

    // ✅ Optional constructor for createOrUpdateProduct (8 params only)
//    public ProductResponseDto(Long id, String name, String productDescription, BigDecimal price,
//                              Integer rewardPoint, String categoryName, String image, String isActive) {
//        this.id = id;
//        this.name = name;
//        this.productDescription = productDescription;
//        this.price = price;
//        this.rewardPoint = rewardPoint;
//        this.categoryName = categoryName;
//        this.image = image;
//        this.isActive = isActive;
//        this.totalQuantity = 0;       // default
//        this.stockStatus = "Unknown"; // default
    }

