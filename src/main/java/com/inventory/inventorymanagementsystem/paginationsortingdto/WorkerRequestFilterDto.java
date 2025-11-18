package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Data;

@Data
public class WorkerRequestFilterDto extends BaseFilterSortDto{
    private String status;  // pending / approved / rejected
    private String search;
}
