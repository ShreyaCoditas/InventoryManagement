package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWorkerRequestDto {

    private Long factoryId;     // optional for Chief Supervisor, required for Plant Head
    @NotBlank private String name;
    @Email @NotBlank
    private String email;

    @NotNull private Long bayId;
}

