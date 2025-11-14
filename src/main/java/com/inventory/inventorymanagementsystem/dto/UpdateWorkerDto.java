package com.inventory.inventorymanagementsystem.dto;

import com.inventory.inventorymanagementsystem.validation.ValidImage;
import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateWorkerDto {

    private Long factoryId; // optional for Chief Supervisor, required for Plant Head


    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @ValidImage
    private MultipartFile imageFile;


    private Long bayId;
}
