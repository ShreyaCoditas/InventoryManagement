package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MerchandiseFilterSortDto extends BaseFilterSortDto{
    private String search;
    private Integer minRewardPoints;
    private Integer maxRewardPoints;
    private String stockStatus; // IN_STOCK, OUT_OF_STOCK
    private String status; // ACTIVE or INACTIVE

}
