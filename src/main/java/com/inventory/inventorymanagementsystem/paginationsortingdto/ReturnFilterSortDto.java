package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReturnFilterSortDto extends BaseFilterSortDto {
    // status list like REQUESTED_RETURN, OVERDUE, SEIZED (optional)
    private List<String> status;
    // search by tool name or worker name
    private String search;
}
