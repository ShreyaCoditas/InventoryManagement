package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;


@Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class WorkerToolRequestItemDto {
       @NotNull(message = "Tool Id to be given")
        private Long toolId;

        @Min(value = 1, message = "quantity to be 1 or more than 1")
        private Integer quantity;
    }


