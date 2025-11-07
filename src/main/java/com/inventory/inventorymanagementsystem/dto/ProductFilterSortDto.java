package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;

@Data
public class ProductFilterSortDto {
    private String categoryName;
    private Boolean inStockOnly; // Optional future use
    private String sortBy; // "priceLowHigh", "priceHighLow", "qtyLowHigh", etc.
}

