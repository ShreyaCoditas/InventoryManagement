
package com.inventory.inventorymanagementsystem.dto;


//import jakarta.mail.Multipart;

//import jakarta.mail.Multipart;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductDto {
    private String name;
    private String productDescription;
    private Long categoryId;
    private String newCategoryName;
    private BigDecimal price;
    private MultipartFile image;
}