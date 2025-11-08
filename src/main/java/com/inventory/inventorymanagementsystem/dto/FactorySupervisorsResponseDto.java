package com.inventory.inventorymanagementsystem.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactorySupervisorsResponseDto {
    private Long supervisorId;
    private String name;
    private String email;
//    private String phoneNumber;
    private String isActive;
}
