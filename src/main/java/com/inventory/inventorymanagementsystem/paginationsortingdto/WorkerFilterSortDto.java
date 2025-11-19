package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WorkerFilterSortDto extends BaseFilterSortDto {
    private String location;  // Filter by factory location (city)
    private List<String> status;
    private List<String> locations; // ACTIVE / INACTIVE
    private String search;

}

