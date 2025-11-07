package com.inventory.inventorymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChiefSupervisorResponseDto {
    private Long supervisorId;
    private String name;
    private String email;
    private String phoneNumber;
    private String factoryId;

    private String isActive;

}
