package com.inventory.inventorymanagementsystem.dto;
import com.inventory.inventorymanagementsystem.validation.ValidImage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AddMerchandiseDto {

//    @NotBlank
//    private String name;
//
//    @NotBlank
//    private String image;
//
//    @NotNull
//    @Min(1)
//    private Integer rewardPoints;
//
//    @NotNull
//    @Min(0)
//    private Integer quantity;
  @NotBlank
    private String name;
   @NotNull
   @Min(1)
    private Long requiredPoints;
    @NotNull
  @Min(0)
    private Long availableQuantity;
    @ValidImage
    private MultipartFile image;


}

