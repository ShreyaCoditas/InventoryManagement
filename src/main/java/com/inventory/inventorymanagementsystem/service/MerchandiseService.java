package com.inventory.inventorymanagementsystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.dto.AddMerchandiseDto;
import com.inventory.inventorymanagementsystem.dto.MerchandiseResponseDto;
import com.inventory.inventorymanagementsystem.dto.UpdateMerchandiseDto;
import com.inventory.inventorymanagementsystem.entity.Merchandise;
import com.inventory.inventorymanagementsystem.entity.User;
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
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Normalize input name
        String normalizedInput = normalizeName(dto.getName());

// Fetch all merchandise once
        List<Merchandise> all = merchandiseRepository.findAll();

        boolean exists = all.stream()
                .anyMatch(m -> normalizeName(m.getName()).equals(normalizedInput));

        if (exists) {
            return new ApiResponseDto<>(false, "Merchandise with this name already exists", null);
        }


        // Validate image
        MultipartFile image = dto.getImage();
        if (image == null || image.isEmpty()) {
            return new ApiResponseDto<>(false, "Image is required", null);
        }

        // Upload image to Cloudinary
        Cloudinary cloudinary = cloudinaryService.getCloudinary();
        Map uploadResult = cloudinary.uploader().upload(
                image.getBytes(),
                ObjectUtils.asMap("folder", "merchandise")
        );
        String imageUrl = (String) uploadResult.get("secure_url");

        // Save merchandise
        Merchandise merchandise = new Merchandise();
        merchandise.setName(dto.getName());
        merchandise.setRewardPoints(dto.getRequiredPoints());
        merchandise.setQuantity(dto.getAvailableQuantity());
        merchandise.setImage(imageUrl);
        merchandise.setIsActive(ActiveStatus.ACTIVE);
        merchandise.setCreatedAt(LocalDateTime.now());

        Merchandise saved = merchandiseRepository.save(merchandise);

//        MerchandiseResponseDto response = new MerchandiseResponseDto(
//                saved.getId(),
//                saved.getName(),
//                saved.getRewardPoints(),
//                saved.getQuantity(),
//                saved.getImage(),
//
//        );

        return new ApiResponseDto<>(true, "Merchandise added successfully", null);
    }



//
//    public ApiResponseDto<Page<MerchandiseResponseDto>> getAllMerchandise(
//            int page,
//            int size,
//            String search,
//            Integer minRewardPoints,
//            Integer maxRewardPoints,
//            String stockStatus,
//            String sort) {
//
//        Pageable pageable;
//
//        // ✅ Sorting
//        if ("rewardPointsAsc".equalsIgnoreCase(sort)) {
//            pageable = PageRequest.of(page, size, Sort.by("rewardPoints").ascending());
//        } else if ("rewardPointsDesc".equalsIgnoreCase(sort)) {
//            pageable = PageRequest.of(page, size, Sort.by("rewardPoints").descending());
//        } else {
//            pageable = PageRequest.of(page, size, Sort.by("id").descending());
//        }
//
//        // ✅ Initialize Specification
//        Specification<Merchandise> spec = (root, query, cb) -> cb.conjunction();
//
//        // ✅ Filter by Active Merchandise
//        spec = spec.and((root, query, cb) ->
//                cb.equal(root.get("isActive"), ActiveStatus.ACTIVE)
//        );
//
//        // ✅ Search by Name
//        if (search != null && !search.isBlank()) {
//            spec = spec.and(MerchandiseSpecifications.searchByName(search));
//        }
//
//        // ✅ Filter by Reward Points
//        if (minRewardPoints != null) {
//            spec = spec.and(MerchandiseSpecifications.hasMinRewardPoints(minRewardPoints));
//        }
//
//        if (maxRewardPoints != null) {
//            spec = spec.and(MerchandiseSpecifications.hasMaxRewardPoints(maxRewardPoints));
//        }
//
//        // ✅ Filter by Stock Status
//        if (stockStatus != null) {
//            if (stockStatus.equalsIgnoreCase("IN_STOCK")) {
//                spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("quantity"), 0));
//            } else if (stockStatus.equalsIgnoreCase("OUT_OF_STOCK")) {
//                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("quantity"), 0));
//            }
//        }
//
//        // ✅ Fetch from DB
//        Page<Merchandise> merchandisePage = merchandiseRepository.findAll(spec, pageable);
//
//        // ✅ Map to DTO
//        Page<MerchandiseResponseDto> dtoPage = merchandisePage.map(m -> new MerchandiseResponseDto(
//                m.getId(),
//                m.getName(),
//                m.getRewardPoints(),
//                m.getQuantity(),
//                m.getImage()
//        ));
//
//        return new ApiResponseDto<>(true, "Filtered merchandise fetched successfully", dtoPage);
//    }
//
//

    public ApiResponseDto<List<MerchandiseResponseDto>> getAllMerchandise(MerchandiseFilterSortDto filter) {

        // PAGE + SORT
        Sort sort = filter.getSortDirection().equalsIgnoreCase("desc") ?
                Sort.by(filter.getSortBy()).descending() :
                Sort.by(filter.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // BASE SPEC
        Specification<Merchandise> spec = Specification.allOf(
                (root, query, cb) -> cb.equal(root.get("isActive"), ActiveStatus.ACTIVE)
        );

        // SEARCH
        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            spec = spec.and(MerchandiseSpecifications.searchByName(filter.getSearch()));
        }

        // MIN REWARD
        if (filter.getMinRewardPoints() != null) {
            spec = spec.and(MerchandiseSpecifications.hasMinRewardPoints(filter.getMinRewardPoints()));
        }

        // MAX REWARD
        if (filter.getMaxRewardPoints() != null) {
            spec = spec.and(MerchandiseSpecifications.hasMaxRewardPoints(filter.getMaxRewardPoints()));
        }

        // STOCK CHECK
        if (filter.getStockStatus() != null) {
            if (filter.getStockStatus().equalsIgnoreCase("INSTOCK")) {
                spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("quantity"), 0));
            } else if (filter.getStockStatus().equalsIgnoreCase("OUTOFSTOCK")) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("quantity"), 0));
            }
        }

        Page<Merchandise> pageResult = merchandiseRepository.findAll(spec, pageable);

        // DTO MAPPING WITH STOCK STATUS
        List<MerchandiseResponseDto> dtos = pageResult.getContent()
                .stream()
                .map(m -> new MerchandiseResponseDto(
                        m.getId(),
                        m.getName(),
                        m.getRewardPoints(),
                        m.getQuantity(),
                        m.getImage(),
                        m.getQuantity() != null && m.getQuantity() > 0 ? "INSTOCK" : "OUTOFSTOCK"
                ))
                .toList();

        Map<String, Object> pagination = PaginationUtil.build(pageResult);

        return new ApiResponseDto<>(
                true,
                "Filtered merchandise fetched successfully",
                dtos,
                pagination
        );
    }



    public ApiResponseDto<Void> softDeleteMerchandise(Long id) {
        Merchandise merchandise = merchandiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchandise not found"));

        if (merchandise.getIsActive() == ActiveStatus.INACTIVE ){
            return new ApiResponseDto<>(false, "Merchandise already deleted", null);
        }

        merchandise.setIsActive(ActiveStatus.ACTIVE);
        merchandiseRepository.save(merchandise);

        return new ApiResponseDto<>(true, "Merchandise deleted successfully ", null);
    }


    public ApiResponseDto<MerchandiseResponseDto> updateMerchandise(Long id, UpdateMerchandiseDto dto) throws Exception {

        Merchandise merchandise = merchandiseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Merchandise not found"));

        // 🔹 1. Update NAME only if provided
        if (dto.getName() != null && !dto.getName().isBlank()) {

            String normalizedInput = normalizeName(dto.getName());

            // Fetch all items for duplicate check
            List<Merchandise> all = merchandiseRepository.findAll();

            boolean exists = all.stream()
                    .filter(m -> !m.getId().equals(id)) // avoid comparing with itself
                    .anyMatch(m -> normalizeName(m.getName()).equals(normalizedInput));

            if (exists) {
                return new ApiResponseDto<>(false, "Another merchandise with similar name already exists", null);
            }

            merchandise.setName(dto.getName());
        }

        // 🔹 2. Update required points (optional)
        if (dto.getRequiredPoints() != null) {
            merchandise.setRewardPoints(dto.getRequiredPoints());
        }

        // 🔹 3. Update available quantity (optional)
        if (dto.getAvailableQuantity() != null) {
            merchandise.setQuantity(dto.getAvailableQuantity());
        }

        // 🔹 4. OPTIONAL image update
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {

            // Upload
            Cloudinary cloudinary = cloudinaryService.getCloudinary();
            Map uploadResult = cloudinary.uploader().upload(
                    dto.getImageFile().getBytes(),
                    ObjectUtils.asMap("folder", "merchandise")
            );
            String imageUrl = (String) uploadResult.get("secure_url");

            merchandise.setImage(imageUrl);
        }

        Merchandise saved = merchandiseRepository.save(merchandise);

//        MerchandiseResponseDto response = new MerchandiseResponseDto(
//                saved.getId(),
//                saved.getName(),
//                saved.getRewardPoints(),
//                saved.getQuantity(),
//                saved.getImage()
//        );

        return new ApiResponseDto<>(true, "Merchandise updated successfully", null);
    }


    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", "").toLowerCase();
    }



//    public ApiResponseDto<MerchandiseResponseDto> restockMerchandise(Long id, Long additionalQuantity) {
//        // 🔐 Validate logged-in user
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User user = userRepository.findByEmailIgnoreCase(email)
//                .orElseThrow(()->new RuntimeException("User not found"));
//
//
//
//        if (!(user.getRole().equals(com.inventory.inventorymanagementsystem.constants.RoleName.OWNER) ||
//                user.getRole().equals(RoleName.CENTRALOFFICER))) {
//            return new ApiResponseDto<>(false, "Access denied: Only OWNER or CENTRAL_OFFICE can restock merchandise", null);
//        }
//
//        // ✅ Find merchandise
//        Merchandise merchandise = merchandiseRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Merchandise not found"));
//
//        if (merchandise.getIsActive() == ActiveStatus.INACTIVE) {
//            return new ApiResponseDto<>(false, "Cannot restock inactive merchandise", null);
//        }
//
//        // ✅ Update quantity
//        merchandise.setQuantity(merchandise.getQuantity() + additionalQuantity);
////            merchandise.setUpdatedAt(LocalDateTime.now());
//        merchandiseRepository.save(merchandise);
//
//        // ✅ Build response
//        MerchandiseResponseDto response = new MerchandiseResponseDto(
//                merchandise.getId(),
//                merchandise.getName(),
//                merchandise.getRewardPoints(),
//                merchandise.getQuantity(),
//                merchandise.getImage()
//        );
//
//        return new ApiResponseDto<>(true, "Merchandise restocked successfully", response);
//    }
//


//    public ApiResponseDto<MerchandiseResponseDto> createMerchandise(AddMerchandiseDto dto) {
//        String normalizedName = normalizeName(dto.getName());
//        boolean exists = merchandiseRepository.findAll().stream()
//                .anyMatch(m -> normalizeName(m.getName()).equalsIgnoreCase(normalizedName));
//        if (exists) {
//            return new ApiResponseDto<>(false, "Merchandise already exists with a similar name", null);
//        }
//        Merchandise merchandise = Merchandise.builder()
//                .name(dto.getName().trim())
//                .image(dto.getImage())
//                .rewardPoints(dto.getRewardPoints())
//                .quantity(dto.getQuantity())
//                .isActive(ActiveStatus.ACTIVE)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//        merchandiseRepository.save(merchandise);
//        return new ApiResponseDto<>(true, "Merchandise created successfully", buildResponseDto(merchandise));
//    }
//
//    public ApiResponseDto<MerchandiseResponseDto> updateMerchandise(Long id, AddMerchandiseDto dto) {
//        Merchandise merchandise = merchandiseRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Merchandise not found with id: " + id));
//        if (dto.getName() != null) merchandise.setName(dto.getName());
//        if (dto.getImage() != null) merchandise.setImage(dto.getImage());
//        if (dto.getRewardPoints() != null) merchandise.setRewardPoints(dto.getRewardPoints());
//        if (dto.getQuantity() != null) merchandise.setQuantity(dto.getQuantity());
//
//        merchandise.setUpdatedAt(LocalDateTime.now());
//        merchandiseRepository.save(merchandise);
//
//        return new ApiResponseDto<>(true, "Merchandise updated successfully", buildResponseDto(merchandise));
//    }
//
//    public ApiResponseDto<String> softDeleteMerchandise(Long id) {
//        Merchandise merchandise = merchandiseRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Merchandise not found with id: " + id));
//        if (merchandise.getIsActive() == ActiveStatus.INACTIVE) {
//            return new ApiResponseDto<>(false, "Merchandise already inactive", null);
//        }
//        merchandise.setIsActive(ActiveStatus.INACTIVE);
//        merchandise.setUpdatedAt(LocalDateTime.now());
//        merchandiseRepository.save(merchandise);
//        return new ApiResponseDto<>(true, "Merchandise soft deleted successfully", "INACTIVE");
//    }
//
//
//
//    private String normalizeName(String name) {
//        if (name == null) return "";
//        return name.trim().replaceAll("\\s+", "").toLowerCase();
//    }
//
//    private MerchandiseResponseDto buildResponseDto(Merchandise merchandise) {
//        return MerchandiseResponseDto.builder()
//                .id(merchandise.getId())
//                .name(merchandise.getName())
//                .image(merchandise.getImage())
//                .rewardPoints(merchandise.getRewardPoints())
//                .quantity(merchandise.getQuantity())
//                .status(merchandise.getIsActive().name())
//                .createdAt(merchandise.getCreatedAt())
//                .updatedAt(merchandise.getUpdatedAt())
//                .build();
//    }
}
