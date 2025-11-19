package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailsResponseDto {

    private Long orderId;
    private String orderCode;

    // product info
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal pricePerUnit;

    // quantities
    private Integer requestedQuantity;
    private Integer totalDispatched;
    private Integer remainingQuantity;

    // statuses
    private String orderStatus;
    private String paymentStatus;

    private LocalDateTime orderDate;

    // invoice info
    private Long invoiceId;
    private BigDecimal invoiceAmount;

    private List<OrderBatchDto> batches;
}
