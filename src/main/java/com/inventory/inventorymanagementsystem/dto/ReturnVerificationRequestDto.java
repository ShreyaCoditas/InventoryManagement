package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class ReturnVerificationRequestDto {
    private Long toolReturnId;
    private Integer fitQuantity;
    private Integer unfitQuantity;
}
