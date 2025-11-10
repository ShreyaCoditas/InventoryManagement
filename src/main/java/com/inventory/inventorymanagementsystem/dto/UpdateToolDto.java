package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.IsPerishableEnum;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateToolDto {

    @Size(max = 100, message = "Tool name cannot exceed 100 characters")
    private String name;

    private String description;

    private Long categoryId;

    private String newCategoryName;

    @Pattern(regexp = "^(http|https)://.*$", message = "Image URL must be valid")
    private String imageUrl;

    private IsPerishableEnum isPerishable;

    private ExpensiveEnum isExpensive;

    @Min(value = 1, message = "Threshold must be at least 1")
    @Max(value = 1000, message = "Threshold cannot exceed 1000")
    private Integer threshold;

    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity;   // ✅ Added this field
}
