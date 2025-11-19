package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFactoryRequestDto {

    @NotBlank(message = "Factory name is required")
    @Size(min = 3, max = 100, message = "Factory name must be between 3 and 100 characters")
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9\\s\\-&().,]*$",
            message = "Factory name must start with a letter and can contain letters, numbers, spaces, and symbols (-, &, (, ), ., ,)"
    )
    private String name;

    @NotBlank(message = "City name is required")
    @Size(min = 3, max = 50, message = "City name must be between 3 and 50 characters")
    @Pattern(
            regexp = "^[A-Za-z\\s]+$",
            message = "City name should contain only letters and spaces"
    )
    private String city;

    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 255, message = "Address must be between 10 and 255 characters")
    private String address;

    // Optional — because a factory can exist before assigning a plant head
    @Positive(message = "Plant Head ID must be a positive number")
    private Long plantHeadId;
}
