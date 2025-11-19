package com.inventory.inventorymanagementsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrUpdateCategoryDto {
    @NotBlank(message="categoryname to be given")
    private String categoryName;

    @NotBlank(message = "description to be given")
    private String categoryDescription;
}
