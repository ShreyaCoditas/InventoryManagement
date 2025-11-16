package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.specifications.ToolSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolCategoryRepository categoryRepository;
    private final StorageAreaRepository storageAreaRepository;
    private final ToolStockRepository toolStockRepository;
    private final CloudinaryService cloudinaryService;
    private final FactoryRepository factoryRepository;
    private final UserFactoryMappingRepository userFactoryMappingRepository;

    // CREATE TOOL
    public ApiResponseDto<ToolResponseDto> createTool(ToolDto dto) {
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
        tool.setIsActive(ActiveStatus.ACTIVE);
        tool.setCreatedAt(LocalDateTime.now());
        tool.setUpdatedAt(LocalDateTime.now());
        tool = toolRepository.save(tool);
        return new ApiResponseDto<>(true, "Tool created", toDto(tool));
    }

     //UPDATE TOOL
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
       //if (dto.getAvailableQuantity() != null) tool.setAvailableQuantity(dto.getAvailableQuantity());
        tool.setUpdatedAt(LocalDateTime.now());
        tool = toolRepository.save(tool);
        return new ApiResponseDto<>(true, "Tool updated", toDto(tool));
    }


    public ApiResponseDto<ToolResponseDto> getToolById(Long id) {
        Tool tool = toolRepository.findByIdAndIsActive(id, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Tool not found with ID: " + id));

        ToolResponseDto responseDto = toDto(tool);
        return new ApiResponseDto<>(true, "Tool fetched successfully", responseDto);
    }
//
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
//
    // CATEGORY: GET ALL
    public ApiResponseDto<List<ToolCategoryResponseDto>> getAllCategories() {
        List<ToolCategoryResponseDto> dtos = categoryRepository.findAll()
                .stream()
                .map(this::toCategoryDto)
                .toList();
        return new ApiResponseDto<>(true, "Categories fetched", dtos);
    }
//
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
//
    // CATEGORY: DELETE
    public ApiResponseDto<String> deleteCategory(Long id) {
        ToolCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(cat);
        return new ApiResponseDto<>(true, "Category deleted", null);
    }
//
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


    @Transactional(readOnly = true)
    public ApiResponseDto<List<ToolResponseDto>> getAllTools(
            int page, int size, String sortBy, String sortDir,
            String availability, Long factoryId, List<String> categoryNames) {

        // ✅ Step 1: Normalize sort field
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "id"; // default sort
        }
//        if ("quantity".equalsIgnoreCase(sortBy)) {
//            sortBy = "availableQuantity"; // handled manually later
//        }
        if ("quantity".equalsIgnoreCase(sortBy) || "availableQuantity".equalsIgnoreCase(sortBy)) {
            sortBy = "id"; // prevent JPA from sorting on non-existent field
        }



        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // ✅ Step 2: Build JPA Specification (for DB-level filtering)
        Specification<Tool> spec = Specification.allOf(
                ToolSpecifications.isActive(),
                ToolSpecifications.hasCategories(categoryNames)
        );

        Page<Tool> toolPage = toolRepository.findAll(spec, pageable);
        List<Tool> tools = new ArrayList<>(toolPage.getContent());

        // ✅ Step 3: Manual filter (factory-based availability - non-DB join)
        if (factoryId != null) {
            Set<Long> toolIdsInFactory = storageAreaRepository.findToolIdsByFactoryId(factoryId);
            tools = tools.stream()
                    .filter(t -> toolIdsInFactory.contains(t.getId()))
                    .toList();
        }

        // ✅ Step 4: Map to DTOs
        List<ToolResponseDto> dtos = tools.stream()
                .map(this::toDto)
                .toList();

        // ✅ Step 5: Manual filter (availability computed field)
        if (availability != null) {
            dtos = dtos.stream()
                    .filter(dto ->
                            ("InStock".equalsIgnoreCase(availability) && dto.getAvailableQuantity() > 0) ||
                                    ("OutOfStock".equalsIgnoreCase(availability) && dto.getAvailableQuantity() == 0))
                    .toList();
        }

        // ✅ Step 6: Manual sorting for computed field (quantity)
        if ("availableQuantity".equalsIgnoreCase(sortBy)) {
            dtos = dtos.stream()
                    .sorted("desc".equalsIgnoreCase(sortDir)
                            ? Comparator.comparing(ToolResponseDto::getAvailableQuantity).reversed()
                            : Comparator.comparing(ToolResponseDto::getAvailableQuantity))
                    .toList();
        }

        // ✅ Step 7: Pagination info
        Map<String, Object> pagination = PaginationUtil.build(toolPage);

        // ✅ Step 8: Return response
        return new ApiResponseDto<>(true, "Tools fetched successfully", dtos, pagination);
    }


    public ApiResponseDto<String> addToolStock(AddToolStockDto dto, UserPrincipal currentUser) {
        User plantHead = currentUser.getUser();
        List<UserFactoryMapping> mappings =
                userFactoryMappingRepository.findAllByUser(plantHead);
        Set<Long> allowedFactoryIds = mappings.stream()
                .map(m -> m.getFactory().getId())
                .collect(Collectors.toSet());
        if (!allowedFactoryIds.contains(dto.getFactoryId())) {
            return new ApiResponseDto<>(false, "You are not allowed to update stock for this factory", null);
        }

        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found"));
        Tool tool = toolRepository.findById(dto.getToolId())
                .orElseThrow(() -> new RuntimeException("Tool not found"));
        ToolStock stock = toolStockRepository
                .findByToolIdAndFactoryId(dto.getToolId(), dto.getFactoryId())
                .orElse(null);
        if (stock == null) {
            stock = new ToolStock();
            stock.setFactory(factory);
            stock.setTool(tool);
            stock.setTotalQuantity(dto.getQuantity().longValue());
            stock.setAvailableQuantity(dto.getQuantity().longValue());
            stock.setIssuedQuantity(0L);
        } else {
            stock.setTotalQuantity(stock.getTotalQuantity() + dto.getQuantity());
            stock.setAvailableQuantity(stock.getAvailableQuantity() + dto.getQuantity());
        }
        toolStockRepository.save(stock);
        return new ApiResponseDto<>(true, "Stock updated successfully", null);
    }








    // HELPER: Build Response
    private ToolResponseDto toDto(Tool t) {
        Integer total = toolStockRepository.sumTotalQuantityByToolId(t.getId());
        Integer available = toolStockRepository.sumAvailableQuantityByToolId(t.getId());
        total = total != null ? total : 0;
        available = available != null ? available : 0;
        String stockStatus;
        int threshold = (t.getThreshold() != null ? t.getThreshold() : 0);
        if (available >= threshold) {
            stockStatus = "INSTOCK";
        } else {
            stockStatus = "OUTOFSTOCK";
        }

        return ToolResponseDto.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getToolDescription())
                .categoryName(t.getCategory() != null ? t.getCategory().getCategoryName() : null)
                .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                .imageUrl(t.getImageUrl())
                .isPerishable(t.getIsPerishable() != null ? t.getIsPerishable().name() : null)
                .isExpensive(t.getIsExpensive() != null ? t.getIsExpensive().name() : null)
                .threshold(t.getThreshold())
                .availableQuantity(available)
                .totalQuantity(total.longValue())  // ✅ total quantity added
                .status(t.getIsActive() != null ? t.getIsActive().name() : null)
                .stockStatus(stockStatus)
                .returnWindowDays(30)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
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