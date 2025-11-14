package com.inventory.inventorymanagementsystem.dto;

import lombok.*;


@Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class WorkerToolRequestItemDto {
        private Long toolId;
        private Integer quantity;
    }


