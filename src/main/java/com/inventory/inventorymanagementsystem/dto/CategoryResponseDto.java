package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponseDto {
    private Long id;
    private String categoryName;
    private String categoryDescription;
}

