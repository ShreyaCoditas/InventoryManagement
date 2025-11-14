package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateWorkerRequestDto {

    private Long factoryId; // optional for Chief Supervisor, required for Plant Head

    @NotBlank(message = "Worker name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Image is required")
    private MultipartFile imageFile;

    @NotNull(message = "Bay ID is required")
    private Long bayId;
}