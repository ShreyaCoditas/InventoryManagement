package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ToolCategoryRequestDto {


        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        private String categoryName;

        @Size(max = 255, message = "Category description cannot exceed 255 characters")
        private String categoryDescription;


}
