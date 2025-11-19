package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.ToolIssuanceStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.paginationsortingdto.BaseFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.ReturnFilterSortDto;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.specifications.ToolIssuanceSpecifications;
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
    private final ToolReturnRepository toolReturnRepository;
    private final ToolIssuanceRepository toolIssuanceRepository;

    // Create Tool
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

     //Update Tool
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

    //To get Tool By Id
    public ApiResponseDto<ToolResponseDto> getToolById(Long id) {
        Tool tool = toolRepository.findByIdAndIsActive(id, ActiveStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Tool not found with ID: " + id));

        ToolResponseDto responseDto = toDto(tool);
        return new ApiResponseDto<>(true, "Tool fetched successfully", responseDto);
    }

    // Soft delete
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

    // To get all categories
    public ApiResponseDto<List<ToolCategoryResponseDto>> getAllCategories() {
        List<ToolCategoryResponseDto> dtos = categoryRepository.findAll()
                .stream()
                .map(this::toCategoryDto)
                .toList();
        return new ApiResponseDto<>(true, "Categories fetched", dtos);
    }

    // To update the category
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

    // To delete tool category
    public ApiResponseDto<String> deleteCategory(Long id) {
        ToolCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(cat);
        return new ApiResponseDto<>(true, "Category deleted", null);
    }


    @Transactional(readOnly = true)
    public ApiResponseDto<List<ToolResponseDto>> getAllTools(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String availability,
            String search,
            Long factoryId,
            List<String> categoryNames,
            List<String> factoryNames
    ) {

        String originalSortBy = sortBy;
        if ("availableQuantity".equalsIgnoreCase(sortBy)) {
            sortBy = "id";
        }

        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Tool> spec = Specification.allOf(
                ToolSpecifications.isActive(),
                ToolSpecifications.hasCategories(categoryNames),
                ToolSpecifications.hasFactoryNames(factoryNames) ,// NEW
                ToolSpecifications.searchByName(search)
        );

        Page<Tool> toolPage = toolRepository.findAll(spec, pageable);

        List<Tool> tools = new ArrayList<>(toolPage.getContent());

        if (factoryId != null) {
            Set<Long> toolIdsInFactory = storageAreaRepository.findToolIdsByFactoryId(factoryId);
            tools = tools.stream()
                    .filter(t -> toolIdsInFactory.contains(t.getId()))
                    .toList();
        }


        List<ToolResponseDto> dtos = tools.stream()
                .map(this::toDto)
                .toList();


        if (availability != null) {
            dtos = dtos.stream()
                    .filter(dto ->
                            ("InStock".equalsIgnoreCase(availability) && dto.getAvailableQuantity() > 0)
                                    ||
                                    ("OutOfStock".equalsIgnoreCase(availability) && dto.getAvailableQuantity() == 0)
                    )
                    .toList();
        }


        if ("availableQuantity".equalsIgnoreCase(originalSortBy)) {
            dtos = dtos.stream()
                    .sorted("desc".equalsIgnoreCase(sortDir)
                            ? Comparator.comparing(ToolResponseDto::getAvailableQuantity).reversed()
                            : Comparator.comparing(ToolResponseDto::getAvailableQuantity)
                    )
                    .toList();
        }

        Map<String, Object> pagination = PaginationUtil.build(toolPage);

        return new ApiResponseDto<>(
                true,
                "Tools fetched successfully",
                dtos,
                pagination
        );
    }





    public ApiResponseDto<List<String>> getStorageSlots(Long factoryId) {
        List<StorageArea> areas = storageAreaRepository.findByFactoryId(factoryId);
        List<String> slots = areas.stream()
                .map(a ->  a.getRowNumber() +
                         a.getColumnNumber() +
                         a.getStackLevel() +
                         a.getBucketNumber())
                .toList();
        return new ApiResponseDto<>(true, "Storage slots fetched", slots);
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

    // Worker requests return (submit returned quantity). Worker must be same as issuance's worker.
    public ApiResponseDto<String> requestReturn(WorkerReturnRequestDto dto, UserPrincipal currentUser) {

        User worker = currentUser.getUser();

        ToolIssuance issuance = toolIssuanceRepository.findById(dto.getIssuanceId())
                .orElseThrow(() -> new RuntimeException("Issuance not found"));

        // Validate ownership: issuance.toolRequest.worker == current user
        if (!issuance.getToolRequest().getWorker().getId().equals(worker.getId())) {
            throw new RuntimeException("You cannot request return for an issuance you don't own");
        }

        if (issuance.getTool().getIsPerishable() != null
                && issuance.getTool().getIsPerishable().name().equalsIgnoreCase("YES")) {
            throw new RuntimeException("Perishable tools are not returnable");
        }

        // validate quantity
        int issuedQty = issuance.getQuantity() != null ? issuance.getQuantity() : 0;
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new RuntimeException("Return quantity must be > 0");
        }
        if (dto.getQuantity() > issuedQty) {
            throw new RuntimeException("Return quantity cannot exceed issued quantity");
        }

        // create a ToolReturn entry (requesting; supervisor will verify actual fit/unfit)
        ToolReturn tr = ToolReturn.builder()
                .toolIssuance(issuance)
                .fitQuantity(0)
                .unfitQuantity(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        toolReturnRepository.save(tr);

        // Set issuance status -> REQUESTED_RETURN (worker submitted return)
        issuance.setIssuanceStatus(ToolIssuanceStatus.REQUESTED_RETURN);
        issuance.setRequestedReturnQuantity(dto.getQuantity());
        toolIssuanceRepository.save(issuance);

        return new ApiResponseDto<>(true, "Return requested successfully", null);
    }


    @Transactional(readOnly = true)
    public ApiResponseDto<List<CSReturnListResponseDto>> getReturnsForCS(
            ReturnFilterSortDto filter,
            UserPrincipal currentUser
    ) {
        User cs = currentUser.getUser();

        var mappingOpt = userFactoryMappingRepository.findByUser(cs);
        if (mappingOpt.isEmpty()) {
            throw new RuntimeException("Supervisor not mapped to any factory");
        }
        Long factoryId = mappingOpt.get().getFactory().getId();


        List<ToolIssuanceStatus> statuses;
        if (filter.getStatus() == null || filter.getStatus().isEmpty()) {
            statuses = List.of(
                    ToolIssuanceStatus.ALLOCATED,
                    ToolIssuanceStatus.REQUESTED_RETURN,
                    ToolIssuanceStatus.OVERDUE
                    //ToolIssuanceStatus.SEIZED
            );
        } else {
            statuses = filter.getStatus().stream()
                    .map(s -> ToolIssuanceStatus.valueOf(s.toUpperCase()))
                    .collect(Collectors.toList());
        }


        Specification<ToolIssuance> spec = Specification.allOf(
                ToolIssuanceSpecifications.belongsToFactory(factoryId),
                ToolIssuanceSpecifications.hasStatuses(statuses),
                // search by tool or worker name (if provided)
                ToolIssuanceSpecifications.searchByToolOrWorker(filter.getSearch())
        );


        Sort sort = "desc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.by(filter.getSortBy()).descending()
                : Sort.by(filter.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);


        Page<ToolIssuance> page = toolIssuanceRepository.findAll(spec, pageable);


        List<CSReturnListResponseDto> dtos = page.getContent().stream().map(iss -> {
            String toolName = iss.getTool() != null ? iss.getTool().getName() : null;
            String toolImage = iss.getTool() != null ? iss.getTool().getImageUrl() : null;
          //  Integer qty = iss.getQuantity() != null ? iss.getQuantity() : 0;

            Integer requestedQty = iss.getQuantity(); // worker initially requested
            Integer returnedQty = iss.getRequestedReturnQuantity(); // worker RETURNED this much

            if (returnedQty == null) returnedQty = 0;

            String workerName = (iss.getToolRequest() != null && iss.getToolRequest().getWorker() != null)
                    ? iss.getToolRequest().getWorker().getUsername() : null;
            String workerImage = (iss.getToolRequest() != null && iss.getToolRequest().getWorker() != null)
                    ? iss.getToolRequest().getWorker().getProfileImage() : null;

            return new CSReturnListResponseDto(
                    iss.getId(),
                    toolName,
                    toolImage,
                    requestedQty,
                    returnedQty,
                    workerName,
                    workerImage,
                    iss.getIssuanceStatus(),
                    iss.getCreatedAt()
            );
        }).collect(Collectors.toList());

        Map<String, Object> pagination = PaginationUtil.build(page);

        return new ApiResponseDto<>(true, "Returns fetched successfully", dtos, pagination);
    }


    // CS: verify a return (supervisor inspects and marks fit/unfit)
    public ApiResponseDto<String> verifyReturn(CSReturnVerificationDto dto, UserPrincipal currentUser) {
        User cs = currentUser.getUser();

        ToolIssuance issuance = toolIssuanceRepository.findById(dto.getIssuanceId())
                .orElseThrow(() -> new RuntimeException("Issuance not found"));

        UserFactoryMapping supMap = userFactoryMappingRepository.findByUser(cs)
                .orElseThrow(() -> new RuntimeException("Supervisor not mapped to factory"));
        Long supFactoryId = supMap.getFactory().getId();

        Long issuanceFactoryId = userFactoryMappingRepository.findFactoryIdByUserId(
                issuance.getToolRequest().getWorker().getId()
        ).orElseThrow(() -> new RuntimeException("Issuance worker not mapped to factory"));

        if (!Objects.equals(supFactoryId, issuanceFactoryId)) {
            throw new RuntimeException("Not authorized to verify this return");
        }

        int fit = dto.getFitQuantity() != null ? dto.getFitQuantity() : 0;
        int unfit = dto.getUnfitQuantity() != null ? dto.getUnfitQuantity() : 0;

        int totalVerified = fit + unfit;
        int issuedQty = issuance.getQuantity() != null ? issuance.getQuantity() : 0;
        // for simplicity we allow verifying up to issuedQty (not enforcing requested-vs-issued split)
        if (totalVerified > issuedQty) {
            throw new RuntimeException("Verified quantity cannot exceed issued quantity");
        }

        // Save ToolReturn record
        ToolReturn tr = ToolReturn.builder()
                .toolIssuance(issuance)
                .updatedBy(cs)
                .fitQuantity(fit)
                .unfitQuantity(unfit)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        toolReturnRepository.save(tr);

        ToolStock stock = toolStockRepository.findByToolIdAndFactoryId(issuance.getTool().getId(), issuanceFactoryId)
                .orElseThrow(() -> new RuntimeException("Tool stock not found for factory"));


        if (fit > 0) {
            stock.setAvailableQuantity(stock.getAvailableQuantity() + fit);
        }

        if (unfit > 0) {
            stock.setTotalQuantity(Math.max(0L, stock.getTotalQuantity() - (long) unfit));
        }

        stock.setIssuedQuantity(Math.max(0L, stock.getIssuedQuantity() - (long) totalVerified));

        toolStockRepository.save(stock);

        issuance.setIssuanceStatus(ToolIssuanceStatus.COMPLETED);
        toolIssuanceRepository.save(issuance);

        return new ApiResponseDto<>(true, "Return verified", null);
    }

    /**
     * Scheduler: Marks ALLOCATED tools as OVERDUE after 30 days.
     */
    public void processOverdueAndSeize() {

        LocalDateTime now = LocalDateTime.now();
        List<ToolIssuance> all = toolIssuanceRepository.findAll();
        for (ToolIssuance iss : all) {

            if (iss.getIssuanceStatus() == ToolIssuanceStatus.COMPLETED) {
                continue;
            }

            if (iss.getIssuanceStatus() == ToolIssuanceStatus.REQUESTED_RETURN) {
                continue;
            }

            if (iss.getTool().getIsPerishable() != null &&
                    iss.getTool().getIsPerishable().name().equalsIgnoreCase("YES")) {
                continue;
            }

            LocalDateTime issuedAt = iss.getIssuedAt();
            if (issuedAt == null) continue;

//            LocalDateTime dueDate = issuedAt.plusDays(30);
            // TESTING ONLY: overdue after 5 minutes
            LocalDateTime dueDate = issuedAt.plusMinutes(5);


            // Mark overdue
            if (now.isAfter(dueDate)) {
                iss.setIssuanceStatus(ToolIssuanceStatus.OVERDUE);
            }
        }

        toolIssuanceRepository.saveAll(all);
    }






    // HELPER: Resolve Category
//    private ToolCategory resolveCategory(Long id, String name) {
//        if (id != null) {
//            return categoryRepository.findById(id)
//                    .orElseThrow(() -> new RuntimeException("Invalid category ID"));
//        }
//        if (name == null || name.isBlank()) {
//            throw new RuntimeException("Category required");
//        }
//        return categoryRepository.findByCategoryNameIgnoreCase(name.trim())
//                .orElseGet(() -> {
//                    ToolCategory newCat = new ToolCategory();
//                    newCat.setCategoryName(name.trim());
//                    newCat.setCategoryDescription("Auto-created");
//                    newCat.setCreatedAt(LocalDateTime.now());
//                    newCat.setUpdatedAt(LocalDateTime.now());
//                    return categoryRepository.save(newCat);
//                });
//    }
    private ToolCategory resolveCategory(Long id, String name) {

        if (id != null && name != null && !name.isBlank()) {
            throw new RuntimeException("Provide either categoryId OR newCategoryName, not both");
        }

        if (id == null && (name == null || name.isBlank())) {
            throw new RuntimeException("Category is required: provide categoryId or newCategoryName");
        }

        if (id != null) {
            return categoryRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Invalid category ID"));
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
    private ToolResponseDto toDto(Tool t) {
        Integer total = toolStockRepository.sumTotalQuantityByToolId(t.getId());
        Integer available = toolStockRepository.sumAvailableQuantityByToolId(t.getId());
        total = total != null ? total : 0;
        available = available != null ? available : 0;
        Integer returnWindowDays =
                t.getIsPerishable().name().equalsIgnoreCase("NO")
                        ? 30     // Only NON-perishable tools get 30 days
                        : null;  // Perishable tools have no return window
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
                //.returnWindowDays(30)
                .returnWindowDays(returnWindowDays)
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