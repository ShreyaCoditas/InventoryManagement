package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.IsPerishableEnum;
import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class ToolRequestDto {

    @NotBlank(message = "Tool name is required")
    @Size(max = 100, message = "Tool name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Tool description is required")
    private String description;

    // Either categoryId OR newCategoryName is required (validated in service)
    private Long categoryId;

    @Size(max = 100, message = "New category name cannot exceed 100 characters")
    private String newCategoryName;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^(http|https)://.*$", message = "Image URL must be valid")
    private String imageUrl;

    @NotNull(message = "Perishable status is required")
    private IsPerishableEnum isPerishable;

    @NotNull(message = "Expensive status is required")
    private ExpensiveEnum isExpensive;

    @NotNull(message = "Threshold value is required")
    @Min(value = 1, message = "Threshold must be at least 1")
    @Max(value = 1000, message = "Threshold cannot exceed 1000")
    private Integer threshold;


}
