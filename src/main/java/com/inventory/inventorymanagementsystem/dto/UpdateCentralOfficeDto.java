package com.inventory.inventorymanagementsystem.dto;



import lombok.Data;

@Data
public class UpdateCentralOfficeDto {
    private Long id;                    // Central Office ID
    private String location;            // New location (optional)
    private String centralOfficeHeadEmail;  // Optional update of head officer
    private String centralOfficeHeadName;   // Optional
}

