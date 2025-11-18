package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateCentralOfficerDto {
    private String name;

    @Email(message="please provide a valid email ID Address")
    private String email;
}

