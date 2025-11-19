package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
@Data
public class AddCentralOfficerDto {

    @NotBlank(message = "Central Officer name is required")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9.\\- ]{1,28}[A-Za-z]$",
            message = "Username must be 3–30 characters, start and end with a letter, and contain only letters, numbers, hyphens, dots, or spaces (no underscores or trailing digits)"
    )
    private String name;



    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._%+-]{0,63}@[A-Za-z][A-Za-z0-9.-]*\\.[A-Za-z]{2,}$",
            message = "Invalid email address"
    )
    private String email;
}
