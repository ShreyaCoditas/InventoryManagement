package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolCategoryRepository categoryRepository;
    private final StorageAreaRepository storageAreaRepository;
    private final CloudinaryService cloudinaryService;

    // CREATE TOOL
    public ApiResponseDto<ToolResponseDto> createTool(ToolRequestDto dto) {
        if (toolRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            return new ApiResponseDto<>(false, "Tool already exists", null);
        }
        String imageUrl = cloudinaryService.uploadFile(dto.getImageFile());
        ToolCategory category = resolveCategory(dto.getCategoryId(), dto.getNewCategoryName());
        Tool tool = new Tool();
        tool.setName(dto.getName().trim());
        tool.setToolDescription(dto.getDescription());
        tool.setCategory(category);
        tool.setImageUrl(imageUrl);
        tool.setIsPerishable(dto.getIsPerishable());
        tool.setIsExpensive(dto.getIsExpensive());
        tool.setThreshold(dto.getThreshold());
        tool.setAvailableQuantity(0);
        tool.setIsActive(ActiveStatus.ACTIVE);
        tool.setCreatedAt(LocalDateTime.now());
        tool.setUpdatedAt(LocalDateTime.now());
        tool = toolRepository.save(tool);
        return new ApiResponseDto<>(true, "Tool created", buildResponse(tool));
    }

    // UPDATE TOOL
    public ApiResponseDto<ToolResponseDto> updateTool(Long id, UpdateToolDto dto) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tool not found"));
        if (dto.getName() != null && !dto.getName().isBlank()) {
            String newName = dto.getName().trim();
            if (!newName.equalsIgnoreCase(tool.getName()) &&
                    toolRepository.existsByNameIgnoreCase(newName)) {
                return new ApiResponseDto<>(false, "Name already taken", null);
            }
            tool.setName(newName);
        }
        if (dto.getCategoryId() != null || (dto.getNewCategoryName() != null && !dto.getNewCategoryName().isBlank())) {
            tool.setCategory(resolveCategory(dto.getCategoryId(), dto.getNewCategoryName()));
        }
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            if (tool.getImageUrl() != null) {
                String publicId = cloudinaryService.extractPublicId(tool.getImageUrl());
                cloudinaryService.delete(publicId);
            }
            tool.setImageUrl(cloudinaryService.uploadFile(dto.getImageFile()));
        }
        if (dto.getDescription() != null) tool.setToolDescription(dto.getDescription());
        if (dto.getThreshold() != null) tool.setThreshold(dto.getThreshold());
        if (dto.getIsPerishable() != null) tool.setIsPerishable(dto.getIsPerishable());
        if (dto.getIsExpensive() != null) tool.setIsExpensive(dto.getIsExpensive());
        if (dto.getAvailableQuantity() != null) tool.setAvailableQuantity(dto.getAvailableQuantity());
        tool.setUpdatedAt(LocalDateTime.now());
        tool = toolRepository.save(tool);
        return new ApiResponseDto<>(true, "Tool updated", buildResponse(tool));
    }

//    // GET ALL TOOLS
//    public ApiResponseDto<List<ToolResponseDto>> getAllTools(
//            int page, int size, String sortBy, String sortDir, String availability) {
//        Sort sort = "desc".equalsIgnoreCase(sortDir)
//                ? Sort.by(sortBy).descending()
//                : Sort.by(sortBy).ascending();
//        Pageable pageable = PageRequest.of(page, size, sort);
//        Page<Tool> toolPage = toolRepository.findByIsActive(ActiveStatus.ACTIVE, pageable);
//        List<ToolResponseDto> dtos = toolPage.getContent().stream()
//                .filter(tool -> {
//                    if (availability == null) return true;
//                    boolean inStock = tool.getAvailableQuantity() > 0;
//                    return "InStock".equalsIgnoreCase(availability) ? inStock : !inStock;
//                })
//                .map(this::buildResponse)
//                .toList();
//        Map<String, Object> pagination = PaginationUtil.build(toolPage); // original count
//        return new ApiResponseDto<>(true, "Tools fetched", dtos, pagination);
//    }

    public ApiResponseDto<List<ToolResponseDto>> getAllTools(
            int page, int size, String sortBy, String sortDir,
            String availability, Long factoryId) {

        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // 1. Get active tools (always)
        Page<Tool> toolPage = toolRepository.findByIsActive(ActiveStatus.ACTIVE, pageable);

        // 2. If factoryId supplied → filter tools that have at least one storage in that factory
        List<Tool> filteredTools = toolPage.getContent();
        if (factoryId != null) {
            Set<Long> toolIdsInFactory = storageAreaRepository
                    .findToolIdsByFactoryId(factoryId);               // ← custom query
            filteredTools = filteredTools.stream()
                    .filter(t -> toolIdsInFactory.contains(t.getId()))
                    .toList();
        }

        // 3. Build DTOs + availability filter
        List<ToolResponseDto> dtos = filteredTools.stream()
                .map(this::buildResponse)
                .filter(dto -> availability == null ||
                        ("InStock".equalsIgnoreCase(availability) && dto.getAvailableQuantity() > 0) ||
                        ("OutOfStock".equalsIgnoreCase(availability) && dto.getAvailableQuantity() == 0))
                .toList();

        // 4. Manual sort by quantity (DB cannot sort on aggregated column)
        if ("quantity".equalsIgnoreCase(sortBy)) {
            dtos.sort("desc".equalsIgnoreCase(sortDir)
                    ? Comparator.comparing(ToolResponseDto::getAvailableQuantity).reversed()
                    : Comparator.comparing(ToolResponseDto::getAvailableQuantity));
        }

        Map<String, Object> pagination = PaginationUtil.build(toolPage);
        return new ApiResponseDto<>(true, "Tools fetched", dtos, pagination);
    }

    public ApiResponseDto<ToolResponseDto> getToolById(Long id) {
        Tool tool = toolRepository.findByIdAndIsActive(id, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Tool not found with ID: " + id));

        ToolResponseDto responseDto = buildResponse(tool);
        return new ApiResponseDto<>(true, "Tool fetched successfully", responseDto);
    }

    // SOFT DELETE
    public ApiResponseDto<String> softDeleteTool(Long id) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tool not found"));

        if (tool.getIsActive() == ActiveStatus.INACTIVE) {
            return new ApiResponseDto<>(false, "Already inactive", null);
        }
        tool.setIsActive(ActiveStatus.INACTIVE);
        tool.setUpdatedAt(LocalDateTime.now());
        toolRepository.save(tool);
        return new ApiResponseDto<>(true, "Tool deleted", "INACTIVE");
    }

    // CATEGORY: GET ALL
    public ApiResponseDto<List<ToolCategoryResponseDto>> getAllCategories() {
        List<ToolCategoryResponseDto> dtos = categoryRepository.findAll()
                .stream()
                .map(this::toCategoryDto)
                .toList();
        return new ApiResponseDto<>(true, "Categories fetched", dtos);
    }

    // CATEGORY: UPDATE
    public ApiResponseDto<ToolCategoryResponseDto> updateCategory(Long id, ToolCategoryRequestDto dto) {
        ToolCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (dto.getCategoryName() != null && !dto.getCategoryName().isBlank()) {
            category.setCategoryName(dto.getCategoryName().trim());
        }
        if (dto.getCategoryDescription() != null) {
            category.setCategoryDescription(dto.getCategoryDescription());
        }
        category.setUpdatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);
        return new ApiResponseDto<>(true, "Category updated", toCategoryDto(category));
    }

    // CATEGORY: DELETE
    public ApiResponseDto<String> deleteCategory(Long id) {
        ToolCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(cat);
        return new ApiResponseDto<>(true, "Category deleted", null);
    }

    // HELPER: Resolve Category
    private ToolCategory resolveCategory(Long id, String name) {
        if (id != null) {
            return categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invalid category ID"));
        }
        if (name == null || name.isBlank()) {
            throw new RuntimeException("Category required");
        }
        return categoryRepository.findByCategoryNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    ToolCategory newCat = new ToolCategory();
                    newCat.setCategoryName(name.trim());
                    newCat.setCategoryDescription("Auto-created");
                    newCat.setCreatedAt(LocalDateTime.now());
                    newCat.setUpdatedAt(LocalDateTime.now());
                    return categoryRepository.save(newCat);
                });
    }

    // HELPER: Build Response
    private ToolResponseDto buildResponse(Tool tool) {
        int qty = storageAreaRepository.findTotalAvailableQuantityByToolId(tool.getId());
        String stockStatus = tool.getAvailableQuantity() > 0 ? "InStock" : "OutOfStock";
        return ToolResponseDto.builder()
                .id(tool.getId())
                .name(tool.getName())
                .description(tool.getToolDescription())
                .categoryName(tool.getCategory().getCategoryName())
                .imageUrl(tool.getImageUrl())
                .isPerishable(tool.getIsPerishable().name())
                .isExpensive(tool.getIsExpensive().name())
                .threshold(tool.getThreshold())
                .availableQuantity(tool.getAvailableQuantity())
                .status(tool.getIsActive().name())
                .stockStatus(stockStatus)
                .createdAt(tool.getCreatedAt())
                .updatedAt(tool.getUpdatedAt())
                .build();
    }

    private ToolCategoryResponseDto toCategoryDto(ToolCategory cat) {
        return ToolCategoryResponseDto.builder()
                .id(cat.getId())
                .categoryName(cat.getCategoryName())
                .categoryDescription(cat.getCategoryDescription())
                .build();
    }
}