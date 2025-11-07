package com.inventory.inventorymanagementsystem.dto;


import lombok.Data;

@Data
public class UpdateFactoryRequestDto {
    private Long id;              // Existing factory ID to update
    private String name;
    private String city;
    private String address;
    private Long plantHeadId;     // Optional - reassign to another Plant Head
}

