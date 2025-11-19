package com.inventory.inventorymanagementsystem.dto;
import com.inventory.inventorymanagementsystem.validation.ValidImage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AddMerchandiseDto {

   @NotBlank(message = "name is required")
   private String name;

    @NotNull(message = "Required points must not be null")
    @Min(value = 1, message = "Required points must be at least 1")
    private Long requiredPoints;

    @NotNull(message = "Available quantity must not be null")
    @Min(value = 1, message = "Available quantity must be at least 1")
    private Long availableQuantity;

    @NotNull(message = "image must not be null")
    @ValidImage(message = "Please upload a valid image file (e.g., JPG, PNG)")
    private MultipartFile image;


}

