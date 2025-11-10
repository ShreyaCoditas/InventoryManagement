package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.MerchandiseRequestDto;
import com.inventory.inventorymanagementsystem.dto.MerchandiseResponseDto;
import com.inventory.inventorymanagementsystem.entity.Merchandise;
import com.inventory.inventorymanagementsystem.repository.MerchandiseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchandiseService {

    @Autowired
    private  MerchandiseRepository merchandiseRepository;

    public ApiResponseDto<MerchandiseResponseDto> createMerchandise(MerchandiseRequestDto dto) {


        String normalizedName = normalizeName(dto.getName());
        boolean exists = merchandiseRepository.findAll().stream()
                .anyMatch(m -> normalizeName(m.getName()).equalsIgnoreCase(normalizedName));
        if (exists) {
            return new ApiResponseDto<>(false, "Merchandise already exists with a similar name", null);
        }

        Merchandise merchandise = Merchandise.builder()
                .name(dto.getName().trim())
                .image(dto.getImage())
                .rewardPoints(dto.getRewardPoints())
                .quantity(dto.getQuantity())
                .isActive(ActiveStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        merchandiseRepository.save(merchandise);

        return new ApiResponseDto<>(true, "Merchandise created successfully", buildResponseDto(merchandise));
    }



    public ApiResponseDto<MerchandiseResponseDto> updateMerchandise(Long id, MerchandiseRequestDto dto) {
        Merchandise merchandise = merchandiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchandise not found with id: " + id));

        if (dto.getName() != null) merchandise.setName(dto.getName());
        if (dto.getImage() != null) merchandise.setImage(dto.getImage());
        if (dto.getRewardPoints() != null) merchandise.setRewardPoints(dto.getRewardPoints());
        if (dto.getQuantity() != null) merchandise.setQuantity(dto.getQuantity());

        merchandise.setUpdatedAt(LocalDateTime.now());
        merchandiseRepository.save(merchandise);

        return new ApiResponseDto<>(true, "Merchandise updated successfully", buildResponseDto(merchandise));
    }

    public ApiResponseDto<String> softDeleteMerchandise(Long id) {
        Merchandise merchandise = merchandiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchandise not found with id: " + id));

        if (merchandise.getIsActive() == ActiveStatus.INACTIVE) {
            return new ApiResponseDto<>(false, "Merchandise already inactive", null);
        }

        merchandise.setIsActive(ActiveStatus.INACTIVE);
        merchandise.setUpdatedAt(LocalDateTime.now());
        merchandiseRepository.save(merchandise);
        return new ApiResponseDto<>(true, "Merchandise soft deleted successfully", "INACTIVE");
    }




    private String normalizeName(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", "").toLowerCase();
    }

    private MerchandiseResponseDto buildResponseDto(Merchandise merchandise) {
        return MerchandiseResponseDto.builder()
                .id(merchandise.getId())
                .name(merchandise.getName())
                .image(merchandise.getImage())
                .rewardPoints(merchandise.getRewardPoints())
                .quantity(merchandise.getQuantity())
                .status(merchandise.getIsActive().name())
                .createdAt(merchandise.getCreatedAt())
                .updatedAt(merchandise.getUpdatedAt())
                .build();
    }
}
