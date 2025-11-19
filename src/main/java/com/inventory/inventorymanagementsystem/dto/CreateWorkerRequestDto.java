package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.validation.ValidImage;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateWorkerRequestDto {

    @Positive(message = "ID should be positive")
    private Long factoryId; // optional for Chief Supervisor, required for Plant Head

    @NotBlank(message = "Worker name is required")
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

    @NotNull(message = "Image is required")
    @ValidImage
    private MultipartFile imageFile;

    @NotNull(message = "Bay ID is required")
    private Long bayId;
}