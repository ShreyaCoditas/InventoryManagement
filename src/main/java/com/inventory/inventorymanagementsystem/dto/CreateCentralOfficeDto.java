package com.inventory.inventorymanagementsystem.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CreateCentralOfficeDto {
    private String location;
    private String centralOfficeHeadEmail;
    private String centralOfficeHeadName;
    private String password;
}
