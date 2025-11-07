package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrUpdateProductDto {

    private Long id; // For Edit — null for new

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String productDescription;

    private Long categoryId; // Optional if new category is added

    private String newCategoryName; // Optional if user adds new one

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    private String image;
}

