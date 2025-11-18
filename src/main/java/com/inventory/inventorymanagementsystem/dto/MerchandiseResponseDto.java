package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class MerchandiseResponseDto {
//    private Long id;
//    private String name;
//    private String image;
//    private Integer rewardPoints;
//    private Integer quantity;
//    private String status;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
    private Long id;
    private String name;
    private Long requiredPoints;
    private Long availableQuantity;
    private String imageUrl;
    private String stockStatus;
    private String status;   // NEW FIELD
}

