package com.inventory.inventorymanagementsystem.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private String productDescription;
    private BigDecimal price;
    private Integer rewardPoint;
    private String categoryName;
    private String image; // URL, not MultipartFile
    private String isActive;
    private String StockStatus;
    private Integer quantity;
}