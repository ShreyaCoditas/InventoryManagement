
package com.inventory.inventorymanagementsystem.dto;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductDto {
    private String name;
    private String productDescription;
    private Long categoryId;
    private String newCategoryName;
    private BigDecimal price;
    private String image;
}