package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
@Data
public class AddCentralOfficerDto {
    @NotBlank(message = "Central Officer name is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]$", message = "Username must be 3-30 characters, start with a letter, and contain only letters, numbers, underscores, or hyphens")
    @Size(min = 3, max = 30)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
