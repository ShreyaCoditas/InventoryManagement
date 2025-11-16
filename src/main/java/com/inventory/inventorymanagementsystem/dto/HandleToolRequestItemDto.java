package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HandleToolRequestItemDto {

        private Long itemId;       // ToolRequestItem ID
        private String action;     // APPROVE / REJECT / FORWARD
        private String reason;     // only for REJECT
    // required if REJECT
}
