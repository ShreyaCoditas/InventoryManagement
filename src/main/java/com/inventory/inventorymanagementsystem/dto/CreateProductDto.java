package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.validation.ValidImage;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
public class CreateProductDto {

    private Long id; // null → create, not null → update

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product description is required")
    private String productDescription;

    private Long categoryId;

    private String newCategoryName;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message="Image is required")
    @ValidImage
    private MultipartFile imageFile; // Fixed: MultipartFile
}