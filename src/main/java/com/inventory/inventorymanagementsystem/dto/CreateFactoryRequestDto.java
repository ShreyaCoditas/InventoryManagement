package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFactoryRequestDto {
    private String name;
    private String city;
    private String address;
    private Long plantHeadId; // optional
}
