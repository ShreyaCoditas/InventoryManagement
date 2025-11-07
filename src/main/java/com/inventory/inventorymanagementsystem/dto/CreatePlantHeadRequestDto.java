package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlantHeadRequestDto {
    private String username;
    private String email;

    private Long factoryId; // optional
}

