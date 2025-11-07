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
public class RegisterDto {

    @NotBlank(message = "username cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]$", message = "Username must be 3-30 characters, start with a letter, and contain only letters, numbers, underscores, or hyphens")
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please enter a valid email address")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9._-]*@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
            message = "Email must start with a letter and be valid like example@gmail.com"
    )
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$",
            message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character"
    )
    private String password;
}
