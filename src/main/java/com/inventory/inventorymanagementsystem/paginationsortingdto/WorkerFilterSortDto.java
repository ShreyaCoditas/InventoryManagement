package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkerFilterSortDto extends BaseFilterSortDto {
    private String location;  // Filter by factory location (city)
    private String status;    // ACTIVE / INACTIVE
}

