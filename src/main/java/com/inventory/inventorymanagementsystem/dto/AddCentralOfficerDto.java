package com.inventory.inventorymanagementsystem.dto;

import lombok.Data;
@Data
public class AddCentralOfficerDto {
    private String centralOfficerEmail;  // Email of the new central officer
    private Long centralOfficeId;        // ID of the Central Office to map to
    private String centralOfficeHeadName; // Name of the Central Office head
    private Long phone;          // Password for the new Central Officer
}
