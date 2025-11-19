package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistributorRegisterDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]*$", message = "Invalid username")
    private String username;

    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._%+-]{0,63}@[A-Za-z][A-Za-z0-9.-]*\\.[A-Za-z]{2,}$",
            message = "Invalid email address"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Tax Registration Number (GSTIN) is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
            message = "Invalid GSTIN format")
    private String taxRegistrationNumber;



//    @NotBlank(message = "Password cannot be empty")
//    @Pattern(
//            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
//            message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
//    )
//    private String password;
}
