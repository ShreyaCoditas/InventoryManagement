package com.inventory.inventorymanagementsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolItemActionDto {
    private String action; // APPROVE, REJECT, FORWARD_TO_PH
    private String reason; // only when REJECT
}
