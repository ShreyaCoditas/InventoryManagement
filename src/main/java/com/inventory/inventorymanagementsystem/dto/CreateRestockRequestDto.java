package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRestockRequestDto {
    @NotNull(message = "Tool id should be given")
    private Long toolId;
}
