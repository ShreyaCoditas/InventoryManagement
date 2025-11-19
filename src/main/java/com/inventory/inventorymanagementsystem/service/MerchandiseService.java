package com.inventory.inventorymanagementsystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.AddMerchandiseDto;
import com.inventory.inventorymanagementsystem.dto.MerchandiseResponseDto;
import com.inventory.inventorymanagementsystem.dto.UpdateMerchandiseDto;
import com.inventory.inventorymanagementsystem.entity.Merchandise;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.exceptions.ResourceNotFoundException;
import com.inventory.inventorymanagementsystem.paginationsortingdto.MerchandiseFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.MerchandiseRepository;
import com.inventory.inventorymanagementsystem.repository.UserRepository;
import com.inventory.inventorymanagementsystem.specifications.MerchandiseSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchandiseService {

    @Autowired
    private  MerchandiseRepository merchandiseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public ApiResponseDto<MerchandiseResponseDto> addMerchandise(AddMerchandiseDto dto) throws IOException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String normalizedInput = normalizeName(dto.getName());
        List<Merchandise> all = merchandiseRepository.findAll();
        boolean exists = all.stream()
                .anyMatch(m -> normalizeName(m.getName()).equals(normalizedInput));
        if (exists) {
            return new ApiResponseDto<>(false, "Merchandise with this name already exists", null);
        }
        MultipartFile image = dto.getImage();
        if (image == null || image.isEmpty()) {
            return new ApiResponseDto<>(false, "Image is required", null);
        }
        Cloudinary cloudinary = cloudinaryService.getCloudinary();
        Map uploadResult = cloudinary.uploader().upload(
                image.getBytes(),
                ObjectUtils.asMap("folder", "merchandise"));
        String imageUrl = (String) uploadResult.get("secure_url");

        Merchandise merchandise = new Merchandise();
        merchandise.setName(dto.getName());
        merchandise.setRewardPoints(dto.getRequiredPoints());
        merchandise.setQuantity(dto.getAvailableQuantity());
        merchandise.setImage(imageUrl);
        merchandise.setIsActive(ActiveStatus.ACTIVE);
        merchandise.setCreatedAt(LocalDateTime.now());
        Merchandise saved = merchandiseRepository.save(merchandise);

        return new ApiResponseDto<>(true, "Merchandise added successfully", null);
    }

public ApiResponseDto<List<MerchandiseResponseDto>> getAllMerchandise(MerchandiseFilterSortDto filter) {
    Sort sort = filter.getSortDirection().equalsIgnoreCase("desc") ?
            Sort.by(filter.getSortBy()).descending() :
            Sort.by(filter.getSortBy()).ascending();
    Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
    Specification<Merchandise> spec = Specification.allOf();
    String status = filter.getStatus();
    if (status == null || status.isBlank()) {
        spec = spec.and(MerchandiseSpecifications.hasStatus("ACTIVE"));
    } else {
        spec = spec.and(MerchandiseSpecifications.hasStatus(status));
    }
    if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
        spec = spec.and(MerchandiseSpecifications.searchByName(filter.getSearch()));
    }

    if (filter.getMinRewardPoints() != null) {
        spec = spec.and(MerchandiseSpecifications.hasMinRewardPoints(filter.getMinRewardPoints()));
    }

    if (filter.getMaxRewardPoints() != null) {
        spec = spec.and(MerchandiseSpecifications.hasMaxRewardPoints(filter.getMaxRewardPoints()));
    }

    if (filter.getStockStatus() != null && !filter.getStockStatus().isBlank()) {
        spec = spec.and(MerchandiseSpecifications.hasStockStatus(filter.getStockStatus()));
    }
    Page<Merchandise> pageResult = merchandiseRepository.findAll(spec, pageable);
    List<MerchandiseResponseDto> dtos = pageResult.getContent()
            .stream()
            .map(m -> new MerchandiseResponseDto(
                    m.getId(),
                    m.getName(),
                    m.getRewardPoints(),
                    m.getQuantity(),
                    m.getImage(),
                    m.getQuantity() != null && m.getQuantity() > 0 ? "INSTOCK" : "OUTOFSTOCK",
                    m.getIsActive().name()     // <-- STATUS INCLUDED
            ))
            .toList();
    Map<String, Object> pagination = PaginationUtil.build(pageResult);
    return new ApiResponseDto<>(true, "Filtered merchandise fetched successfully", dtos, pagination);
}



    public ApiResponseDto<Void> softDeleteMerchandise(Long id) {
        Merchandise merchandise = merchandiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchandise not found"));
        if (merchandise.getIsActive() == ActiveStatus.INACTIVE ){
            return new ApiResponseDto<>(false, "Merchandise already deleted", null);
        }
        merchandise.setIsActive(ActiveStatus.INACTIVE);
        merchandiseRepository.save(merchandise);
        return new ApiResponseDto<>(true, "Merchandise deleted successfully ", null);
    }


    public ApiResponseDto<MerchandiseResponseDto> updateMerchandise(Long id, UpdateMerchandiseDto dto) throws Exception {
        Merchandise merchandise = merchandiseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchandise not found"));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            String normalizedInput = normalizeName(dto.getName());
            List<Merchandise> all = merchandiseRepository.findAll();
            boolean exists = all.stream()
                    .filter(m -> !m.getId().equals(id)) // avoid comparing with itself
                    .anyMatch(m -> normalizeName(m.getName()).equals(normalizedInput));
            if (exists) {
                return new ApiResponseDto<>(false, "Another merchandise with similar name already exists", null);
            }
            merchandise.setName(dto.getName());
        }
        if (dto.getRequiredPoints() != null) {
            merchandise.setRewardPoints(dto.getRequiredPoints());
        }
        if (dto.getAvailableQuantity() != null) {
            merchandise.setQuantity(dto.getAvailableQuantity());
        }
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            Cloudinary cloudinary = cloudinaryService.getCloudinary();
            Map uploadResult = cloudinary.uploader().upload(
                    dto.getImageFile().getBytes(),
                    ObjectUtils.asMap("folder", "merchandise")
            );
            String imageUrl = (String) uploadResult.get("secure_url");
            merchandise.setImage(imageUrl);
        }
        Merchandise saved = merchandiseRepository.save(merchandise);
        return new ApiResponseDto<>(true, "Merchandise updated successfully", null);
    }


    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", "").toLowerCase();
    }

}
