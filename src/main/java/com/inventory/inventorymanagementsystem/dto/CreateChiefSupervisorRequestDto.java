package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChiefSupervisorRequestDto {
    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._-]{1,28}[a-zA-Z0-9]$", message = "Username must be 3-30 characters, start with a letter, and contain only letters, numbers, underscores, or hyphens")
    @Size(min = 3, max = 30)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Factory ID is required")
    @Positive(message = "Factory ID must be a positive number")

    private Long factoryId;   // Required - owner or planthead selects factory

}

