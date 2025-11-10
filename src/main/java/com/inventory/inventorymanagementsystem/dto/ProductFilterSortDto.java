package com.inventory.inventorymanagementsystem.dto;


import com.inventory.inventorymanagementsystem.paginationsortingdto.BaseFilterSortDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFilterSortDto extends BaseFilterSortDto {
    private String categoryName;   // Filter by category name
    private String availability;   // "InStock" or "OutOfStock"
    private String sortBy;         // "price" or "quantity"
    private String sortDirection;  // "asc" or "desc"
}

