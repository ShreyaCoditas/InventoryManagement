package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.IsPerishableEnum;
import com.inventory.inventorymanagementsystem.validation.ValidImage;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateToolDto {

    @Size(max = 100, message = "Tool name cannot exceed 100 characters")
    private String name;

    private String description;

    private Long categoryId;

    private String newCategoryName;

    @ValidImage
    private MultipartFile imageFile; // Optional

    private IsPerishableEnum isPerishable;

    private ExpensiveEnum isExpensive;

    @Min(value = 1, message = "Threshold must be at least 1")
    @Max(value = 1000, message = "Threshold cannot exceed 1000")
    private Integer threshold;

    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity;   // ✅ Added this field
}
