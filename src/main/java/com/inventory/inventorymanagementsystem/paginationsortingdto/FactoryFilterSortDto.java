package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FactoryFilterSortDto extends BaseFilterSortDto {
    private String location;        // filter factories by location
    private String plantHeadName;   // filter by plant head assigned
}
