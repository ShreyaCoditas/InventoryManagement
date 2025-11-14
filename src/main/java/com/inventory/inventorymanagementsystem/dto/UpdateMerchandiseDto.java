package com.inventory.inventorymanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpdateMerchandiseDto {
    private String name;              // optional
    private Long requiredPoints;      // optional
    private Long availableQuantity;   // optional
    private MultipartFile imageFile;
}
