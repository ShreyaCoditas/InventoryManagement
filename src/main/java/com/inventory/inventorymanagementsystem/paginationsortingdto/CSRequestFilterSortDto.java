package com.inventory.inventorymanagementsystem.paginationsortingdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CSRequestFilterSortDto extends BaseFilterSortDto {
    private String status;        // PENDING, APPROVED, REJECTED, FORWARDED_TO_PH
    private String expensive;     // YES or NO
}
