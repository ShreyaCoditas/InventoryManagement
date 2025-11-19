package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FactoryFilterSortDto extends BaseFilterSortDto {
    private List<String> location;
    // filter factories by location
    private String plantHeadName;
    private String status; // ACTIVE / INACTIVE
// filter by plant head assigned
private String search;

}
