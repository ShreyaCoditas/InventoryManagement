package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseFilterSortDto {
    private int page = 0; // default page number
    private int size = 10; // default page size
    private String sortBy = "id"; // default sorting field
    private String sortDirection = "asc";
}
