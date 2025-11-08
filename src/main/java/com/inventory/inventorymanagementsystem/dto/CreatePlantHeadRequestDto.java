package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePlantHeadRequestDto {
    @NotBlank(message = "username cannot be blank")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]$", message = "Username must be 3-30 characters, start with a letter, and contain only letters, numbers, underscores, or hyphens")
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Email
    private String email;

    private Long factoryId; // optional
}

