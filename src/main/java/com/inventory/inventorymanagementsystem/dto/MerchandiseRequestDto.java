package com.inventory.inventorymanagementsystem.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MerchandiseRequestDto {

    @NotBlank
    private String name;

    @NotBlank
    private String image;

    @NotNull
    @Min(1)
    private Integer rewardPoints;

    @NotNull
    @Min(0)
    private Integer quantity;
}

