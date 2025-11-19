package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class WorkerReturnRequestDto {
    @NotNull(message = "Id should be given")
    private Long issuanceId;
    @Min(value=1, message = "quantity should be 1 or more than one")
    private Integer quantity; // how many items worker is returning (<= issuance.quantity)
}
