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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolCategoryRepository toolCategoryRepository;
    private final StorageAreaRepository storageAreaRepository;

    // =====================================================
    // ✅ CREATE TOOL (with auto-create category)
    // =====================================================
//    @Transactional
//    public ApiResponseDto<ToolResponseDto> createTool(ToolRequestDto dto) {
//
//        // Resolve category (existing or new)
//        ToolCategory category = resolveCategory(dto.getCategoryId(), dto.getNewCategoryName());
//
////        // Validate storage area
////        StorageArea storageArea = storageAreaRepository.findById(dto.getStorageAreaId())
////                .orElseThrow(() -> new RuntimeException("Invalid Storage Area ID: " + dto.getStorageAreaId()));
//
//        // Prevent duplicate name
//        if (toolRepository.existsByNameIgnoreCase(dto.getName().trim())) {
//            return new ApiResponseDto<>(false, "Tool already exists with this name", null);
//        }
//
//        Tool tool = new Tool();
//        tool.setName(dto.getName().trim());
//        tool.setToolDescription(dto.getDescription());
//        tool.setCategory(category);
//        tool.setImageUrl(dto.getImageUrl());
//        tool.setIsPerishable(dto.getIsPerishable());
//        tool.setIsExpensive(dto.getIsExpensive());
//        tool.setThreshold(dto.getThreshold());
//        tool.setAvailableQuantity(dto.getAvailableQuantity() != null ? dto.getAvailableQuantity() : 0);
//
//        tool.setIsActive(ActiveStatus.ACTIVE);
//        tool.setCreatedAt(LocalDateTime.now());
//        tool.setUpdatedAt(LocalDateTime.now());
//        toolRepository.save(tool);
//
//        // Link tool to storage area
////        storageArea.setTool(tool);
////        storageAreaRepository.save(storageArea);
//
//        ToolResponseDto response = buildToolResponse(tool);
//        return new ApiResponseDto<>(true, "Tool created successfully", response);
//    }

    // =====================================================
    // ✅ UPDATE TOOL
    // =====================================================
    @Transactional
    public ApiResponseDto<ToolResponseDto> updateTool(Long id, UpdateToolDto dto) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tool not found with ID: " + id));

        if (dto.getCategoryId() != null || (dto.getNewCategoryName() != null && !dto.getNewCategoryName().isBlank())) {
            ToolCategory category = resolveCategory(dto.getCategoryId(), dto.getNewCategoryName());
            tool.setCategory(category);
        }

        if (dto.getName() != null) tool.setName(dto.getName().trim());
        if (dto.getDescription() != null) tool.setToolDescription(dto.getDescription());
        if (dto.getImageUrl() != null) tool.setImageUrl(dto.getImageUrl());
        if (dto.getThreshold() != null) tool.setThreshold(dto.getThreshold());
        if (dto.getIsPerishable() != null) tool.setIsPerishable(dto.getIsPerishable());
        if (dto.getIsExpensive() != null) tool.setIsExpensive(dto.getIsExpensive());
        if (dto.getAvailableQuantity() != null) tool.setAvailableQuantity(dto.getAvailableQuantity());


        tool.setUpdatedAt(LocalDateTime.now());
        toolRepository.save(tool);

        ToolResponseDto response = buildToolResponse(tool);
        return new ApiResponseDto<>(true, "Tool updated successfully", response);
    }

    // =====================================================
    // ✅ GET ALL TOOLS (with pagination)
    // =====================================================
    public ApiResponseDto<List<ToolResponseDto>> getAllTools(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(sortBy);
        if ("desc".equalsIgnoreCase(sortDir)) sort = sort.descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Tool> tools = toolRepository.findAll(pageable);
        List<ToolResponseDto> toolList = tools.getContent().stream()
                .map(this::buildToolResponse)
                .toList();

        Map<String, Object> pagination = PaginationUtil.build(tools);
        return new ApiResponseDto<>(true, "Tools fetched successfully", toolList, pagination);
    }

    // =====================================================
    // ✅ SOFT DELETE TOOL
    // =====================================================
    @Transactional
    public ApiResponseDto<String> softDeleteTool(Long id) {
        Tool tool = toolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tool not found with ID: " + id));

        if (tool.getIsActive() == ActiveStatus.INACTIVE)
            return new ApiResponseDto<>(false, "Tool already inactive", null);

        tool.setIsActive(ActiveStatus.INACTIVE);
        tool.setUpdatedAt(LocalDateTime.now());
        toolRepository.save(tool);

        return new ApiResponseDto<>(true, "Tool soft deleted successfully", "INACTIVE");
    }

    // Get all categories
    public ApiResponseDto<List<ToolCategoryResponseDto>> getAllCategories() {
        List<ToolCategory> categories = toolCategoryRepository.findAll();
        List<ToolCategoryResponseDto> result = categories.stream()
                .map(this::buildCategoryResponse)
                .toList();
        return new ApiResponseDto<>(true, "Categories fetched successfully", result);
    }

    // Update category
    @Transactional
    public ApiResponseDto<ToolCategoryResponseDto> updateCategory(Long id, ToolCategoryRequestDto dto) {
        ToolCategory category = toolCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        if (dto.getCategoryName() != null && !dto.getCategoryName().isBlank())
            category.setCategoryName(dto.getCategoryName().trim());
        if (dto.getCategoryDescription() != null)
            category.setCategoryDescription(dto.getCategoryDescription());

        category.setUpdatedAt(LocalDateTime.now());
        toolCategoryRepository.save(category);

        return new ApiResponseDto<>(true, "Category updated successfully", buildCategoryResponse(category));
    }

    // Hard delete category
    @Transactional
    public ApiResponseDto<String> deleteCategory(Long id) {
        ToolCategory category = toolCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));

        toolCategoryRepository.delete(category);
        return new ApiResponseDto<>(true, "Category deleted successfully", null);
    }




    private ToolCategory resolveCategory(Long categoryId, String newCategoryName) {
        if (categoryId != null) {
            return toolCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Invalid category ID: " + categoryId));
        } else if (newCategoryName != null && !newCategoryName.isBlank()) {
            String normalized = newCategoryName.trim();
            return toolCategoryRepository.findByCategoryNameIgnoreCase(normalized)
                    .orElseGet(() -> {
                        ToolCategory newCat = new ToolCategory();
                        newCat.setCategoryName(normalized);
                        newCat.setCategoryDescription("Auto-created from tool form");
                        newCat.setCreatedAt(LocalDateTime.now());
                        newCat.setUpdatedAt(LocalDateTime.now());
                        return toolCategoryRepository.save(newCat);
                    });
        } else {
            throw new RuntimeException("Either categoryId or newCategoryName must be provided");
        }
    }

    private ToolResponseDto buildToolResponse(Tool tool) {
        int availableQty = storageAreaRepository.findTotalAvailableQuantityByToolId(tool.getId());
        return ToolResponseDto.builder()
                .id(tool.getId())
                .name(tool.getName())
                .description(tool.getToolDescription())
                .categoryName(tool.getCategory().getCategoryName())
                .imageUrl(tool.getImageUrl())
                .isPerishable(tool.getIsPerishable().name())
                .isExpensive(tool.getIsExpensive().name())
                .threshold(tool.getThreshold())
                .availableQuantity(availableQty)
                .status(tool.getIsActive().name())
                .createdAt(tool.getCreatedAt())
                .updatedAt(tool.getUpdatedAt())
                .build();
    }

    private ToolCategoryResponseDto buildCategoryResponse(ToolCategory category) {
        return ToolCategoryResponseDto.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .categoryDescription(category.getCategoryDescription())
                .build();
    }
}
