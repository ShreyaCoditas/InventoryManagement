package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CentralOfficeRestockResponseDto {
    private Long id;

    // Factory
    private Long factoryId;
    private String factoryName;

    // Plant Head
  //  private Long plantHeadId;
 //   private String plantHeadName;

    // Product
    private Long productId;
    private String productName;
    private String productImageUrl;

    // Request Details
    private Integer qtyRequested;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // Requested By
    private Long requestedByUserId;
    private String requestedByUserName;

    // Stocks
   // private Long currentFactoryStock;
    //private Long centralOfficeStock;
}
