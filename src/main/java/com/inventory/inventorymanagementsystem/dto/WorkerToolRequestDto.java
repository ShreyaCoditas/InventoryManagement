package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

import java.util.List;



    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class WorkerToolRequestDto {
        private List<WorkerToolRequestItemDto> items;
    }


