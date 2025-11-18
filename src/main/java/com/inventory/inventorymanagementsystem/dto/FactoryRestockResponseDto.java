package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactoryRestockResponseDto {
    private Long id;

    private Long factoryId;
    private String factoryName;

    private Long productId;
    private String productName;
    private String productImageUrl;

    private Integer qtyRequested;        // quantity requested
    private Long currentFactoryStock;        // total available in factory inventory

    private RequestStatus status;
    private LocalDateTime createdAt;

    private Long requestedByUserId;
    private String requestedByUserName;
}
