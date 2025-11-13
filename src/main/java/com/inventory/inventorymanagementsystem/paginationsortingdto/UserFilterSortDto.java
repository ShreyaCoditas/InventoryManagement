package com.inventory.inventorymanagementsystem.paginationsortingdto;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UserFilterSortDto extends BaseFilterSortDto {

    private String name; // search by name
    private ActiveStatus status; // filter active/inactive

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdBefore;

    private List<ActiveStatus> statuses; // instead of single ActiveStatus

}
