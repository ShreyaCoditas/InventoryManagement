package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InvoiceSimpleDto {
    private Long invoiceId;
    private BigDecimal totalAmount;
    private int batchesCount;
    private List<SimpleBatchDto> batches;
}
