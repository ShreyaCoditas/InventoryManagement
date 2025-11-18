package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.RequestStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.paginationsortingdto.BaseFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.specifications.CentralOfficeInventorySpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductRequestService {

    private final CentralOfficeProductRequestRepository requestRepo;
    private final ProductRepository productRepository;
    private final FactoryRepository factoryRepository;
    private final UserRepository userRepository;
    private final CentralOfficeInventoryRepository centralInventoryRepo;
    private final CentralOfficeRepository centralOfficeRepository;
    private final UserCentralOfficeRepository userCentralOfficeRepository;
    private final UserFactoryMappingRepository userFactoryMappingRepository;
    private final FactoryInventoryRepository factoryInventoryRepository;
    private final FactoryProductionRepository factoryProductionRepository;

    /**
     * Central Office user creates restock request for a factory.
     * Returns ApiResponseDto<CentralOfficeRestockResponseDto> with data = null (as requested).
     */
    public ApiResponseDto<CentralOfficeRestockResponseDto> createRestockRequest(
            CreateProductRestockRequestDto dto,
            UserPrincipal currentUser
    ) {
        User user = currentUser.getUser();

        // Validate role
        if (user.getRole() == null || user.getRole().getRoleName() == null ||
                !user.getRole().getRoleName().name().equalsIgnoreCase("CENTRALOFFICER")) {
            throw new AccessDeniedException("Only central officers can create restock requests");
        }

        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));


        CentralOfficeProductRequest request = CentralOfficeProductRequest.builder()
                .factory(factory)
                .product(product)
                .qtyRequested(dto.getQtyRequested())
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // set requestedBy if your entity has such field (some of your entities used requestedBy)
        try {
            request.setRequestedBy(user);
        } catch (Exception ignore) {}

        requestRepo.save(request);

        // Build response DTO (but user asked to return null data for POST/PUT; we'll return message + null payload)
        return new ApiResponseDto<>(true, "Restock request created", null);
    }

    /**
     * PlantHead / Factory manager completes a restock request (moves qty from factory -> central office inventory).
     * For PUT endpoint we return ApiResponseDto with null data (per your requirement).
     */
    @Transactional
    public ApiResponseDto<FactoryRestockResponseDto> completeRestockRequest(
            Long requestId, UserPrincipal currentUser) {

        User user = currentUser.getUser();

        // Validate Plant Head mapping
        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User not mapped to a factory"));
        Long userFactoryId = mapping.getFactory().getId();

        // Fetch request
        CentralOfficeProductRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Restock request not found"));

        if (!Objects.equals(request.getFactory().getId(), userFactoryId)) {
            throw new AccessDeniedException("Not authorized to complete this restock request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Only PENDING requests can be completed");
        }

        Product product = request.getProduct();
        long qty = request.getQtyRequested() != null ? request.getQtyRequested() : 0;

        // ---------------------------------------------------------
        // 1️⃣ Reduce Factory Inventory Stock
        // ---------------------------------------------------------
        Factory factory = request.getFactory();

        FactoryInventoryStock factoryStock = factoryInventoryRepository
                .findByFactoryIdAndProductId(factory.getId(), product.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Factory does not have stock entry for this product"
                ));

        if (factoryStock.getQuantity() < qty) {
            throw new RuntimeException(
                    "Not enough stock in factory. Available: " + factoryStock.getQuantity()
            );
        }

        factoryStock.setQuantity(factoryStock.getQuantity() - (int) qty);
        factoryInventoryRepository.save(factoryStock);


        // ---------------------------------------------------------
        // 2️⃣ Add to Central Office Inventory
        // ---------------------------------------------------------
        CentralOfficeInventory centralInv = centralInventoryRepo.findByProduct(product)
                .orElseGet(() -> {
                    CentralOfficeInventory inv = new CentralOfficeInventory();
                    inv.setProduct(product);
                    inv.setQuantity(0L);
                    inv.setTotalReceived(0L);
                    return inv;
                });

        centralInv.setQuantity(centralInv.getQuantity() + qty);
        centralInv.setTotalReceived(centralInv.getTotalReceived() + qty);
        centralInventoryRepo.save(centralInv);


        // ---------------------------------------------------------
        // 3️⃣ Update Request Status
        // ---------------------------------------------------------
        request.setStatus(RequestStatus.COMPLETED);
        request.setUpdatedAt(LocalDateTime.now());
        request.setCompletedAt(LocalDateTime.now());
        requestRepo.save(request);

        return new ApiResponseDto<>(true, "Restock request completed", null);
    }


    /**
     * Central office inventory listing (search & filters).
     * Default: show all central inventory entries (you can add filters).
     */
//    @Transactional(readOnly = true)
//    public ApiResponseDto<List<CentralOfficeRestockResponseDto>> getCentralOfficeInventory(
//            Long productId, String productName, Long minQuantity, Long maxQuantity,
//            BaseFilterSortDto base
//    ) {
//        // Build Pageable
//        Sort sort = "desc".equalsIgnoreCase(base.getSortDirection())
//                ? Sort.by(base.getSortBy()).descending()
//                : Sort.by(base.getSortBy()).ascending();
//        Pageable pageable = PageRequest.of(base.getPage(), base.getSize(), sort);
//
//        // Build specification for CentralOfficeInventory (if you want specs; otherwise fetch all)
//        Specification<CentralOfficeInventory> spec = (root, query, cb) -> cb.conjunction();
//        if (productId != null) {
//            spec = spec.and((root, query, cb) ->
//                    cb.equal(root.get("product").get("id"), productId));
//        }
//        if (productName != null && !productName.isBlank()) {
//            String like = "%" + productName.toLowerCase() + "%";
//            spec = spec.and((root, query, cb) ->
//                    cb.like(cb.lower(root.get("product").get("name")), like));
//        }
//        if (minQuantity != null) {
//            spec = spec.and((root, query, cb) ->
//                    cb.greaterThanOrEqualTo(root.get("quantity"), minQuantity));
//        }
//        if (maxQuantity != null) {
//            spec = spec.and((root, query, cb) ->
//                    cb.lessThanOrEqualTo(root.get("quantity"), maxQuantity));
//        }
//
//        Page<CentralOfficeInventory> page = centralInventoryRepo.findAll(spec, pageable);
//
//        List<CentralOfficeRestockResponseDto> dtos = page.getContent().stream().map(inv -> {
//            Product p = inv.getProduct();
//            CentralOfficeRestockResponseDto dto = new CentralOfficeRestockResponseDto();
//            dto.setProductId(p.getId());
//            dto.setProductName(p.getName());
//            dto.setProductImageUrl(p.getImage());        // image
//            dto.setCentralOfficeStock(inv.getQuantity());
//            // other fields left null (this endpoint is for inventory)
//            return dto;
//        }).collect(Collectors.toList());
//
//        Map<String, Object> pagination = PaginationUtil.build(page);
//        return new ApiResponseDto<>(true, "Central office inventory fetched", dtos, pagination);
//    }


    /**
     * Central officer: get restock requests created by current central officer
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<CentralOfficeRestockResponseDto>> getMyRestockRequests(
            BaseFilterSortDto base,
            UserPrincipal currentUser,
            RequestStatus statusFilter
    ) {
        User centralOfficer = currentUser.getUser();

        // ===== SORTING =====
        Sort sort = "desc".equalsIgnoreCase(base.getSortDirection())
                ? Sort.by(base.getSortBy()).descending()
                : Sort.by(base.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(base.getPage(), base.getSize(), sort);

        // ===== SPECIFICATION: only get requests created by THIS central officer =====
        Specification<CentralOfficeProductRequest> spec = (root, query, cb) ->
                cb.equal(root.get("requestedBy").get("id"), centralOfficer.getId());

        // Optional filter: status = Pending | Completed
        if (statusFilter != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusFilter));
        }

        Page<CentralOfficeProductRequest> page = requestRepo.findAll(spec, pageable);

        // ===== Convert to DTO =====
        List<CentralOfficeRestockResponseDto> dtos = page.getContent().stream().map(r -> {

            CentralOfficeRestockResponseDto dto = new CentralOfficeRestockResponseDto();

            // ---------- Basic Request Info ----------
            dto.setId(r.getId());
            dto.setQtyRequested(r.getQtyRequested());
            dto.setStatus(r.getStatus());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setCompletedAt(r.getCompletedAt());

            // ---------- Product Info ----------
            if (r.getProduct() != null) {
                dto.setProductId(r.getProduct().getId());
                dto.setProductName(r.getProduct().getName());
                dto.setProductImageUrl(r.getProduct().getImage());
            }

            // ---------- Factory Info ----------
            if (r.getFactory() != null) {
                dto.setFactoryId(r.getFactory().getId());
                dto.setFactoryName(r.getFactory().getName());
            }

            // ---------- Requested By Central Officer ----------
            if (r.getRequestedBy() != null) {
                dto.setRequestedByUserId(r.getRequestedBy().getId());
                dto.setRequestedByUserName(r.getRequestedBy().getUsername());
            }

            // ---------- PLANT HEAD INFO ----------
            // simple repository method: findByFactoryIdAndUserRoleRoleName
//            UserFactoryMapping mapping = userFactoryMappingRepository
//                    .findByFactoryIdAndUserRoleRoleName(
//                            r.getFactory().getId(), RoleName.PLANTHEAD)
//                    .orElse(null);

//            if (mapping != null && mapping.getUser() != null) {
//                dto.setPlantHeadId(mapping.getUser().getId());
//                dto.setPlantHeadName(mapping.getUser().getUsername());
//            }

            return dto;
        }).toList();

        Map<String, Object> pagination = PaginationUtil.build(page);

        return new ApiResponseDto<>(
                true,
                "My restock requests fetched successfully",
                dtos,
                pagination
        );
    }



    /**
     * Factory manager / Plant Head: get restock requests for factory they manage
     */
    @Transactional(readOnly = true)
    public ApiResponseDto<List<FactoryRestockResponseDto>> getMyFactoryRestockRequests(
            BaseFilterSortDto base,
            UserPrincipal currentUser,
            RequestStatus statusFilter
    ) {

        User user = currentUser.getUser();

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("User not mapped to any factory"));

        Long factoryId = mapping.getFactory().getId();

        // Sorting + paging
        Sort sort = "desc".equalsIgnoreCase(base.getSortDirection())
                ? Sort.by(base.getSortBy()).descending()
                : Sort.by(base.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(base.getPage(), base.getSize(), sort);

        // Build base spec → only this factory
        Specification<CentralOfficeProductRequest> spec =
                (root, query, cb) -> cb.equal(root.get("factory").get("id"), factoryId);

        // Add STATUS filter only if provided
        if (statusFilter != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), statusFilter));
        }

        Page<CentralOfficeProductRequest> page = requestRepo.findAll(spec, pageable);

        List<FactoryRestockResponseDto> dtos = page.getContent().stream().map(r -> {

            FactoryRestockResponseDto dto = new FactoryRestockResponseDto();

            dto.setId(r.getId());
            dto.setFactoryId(r.getFactory().getId());
            dto.setFactoryName(r.getFactory().getName());

            dto.setProductId(r.getProduct().getId());
            dto.setProductName(r.getProduct().getName());
            dto.setProductImageUrl(r.getProduct().getImage());

            dto.setQtyRequested(r.getQtyRequested());
            dto.setStatus(r.getStatus());
            dto.setCreatedAt(r.getCreatedAt());

            // Requested by user fields
            if (r.getRequestedBy() != null) {
                dto.setRequestedByUserId(r.getRequestedBy().getId());
                dto.setRequestedByUserName(r.getRequestedBy().getUsername());
            }

            // Clean stock fetch (simplified)
            FactoryInventoryStock inv = factoryInventoryRepository
                    .findByFactoryIdAndProductId(
                            r.getFactory().getId(),
                            r.getProduct().getId()
                    )
                    .orElse(null);

            Long currentFactoryStock = inv != null ? inv.getQuantity().longValue() : 0L;
            dto.setCurrentFactoryStock(currentFactoryStock);

            return dto;
        }).toList();


        Map<String, Object> pagination = PaginationUtil.build(page);
        return new ApiResponseDto<>(true, "Factory restock requests fetched", dtos, pagination);
    }



    @Transactional
    public ApiResponseDto<String> addProductStockToFactory(
            AddProductStockDto dto,
            UserPrincipal currentUser
    ) {
        User plantHead = currentUser.getUser();

        // 1. Get all factories assigned to this PlantHead
        List<UserFactoryMapping> mappings =
                userFactoryMappingRepository.findAllByUser(plantHead);

        if (mappings.isEmpty()) {
            throw new RuntimeException("Plant Head is not assigned to any factory");
        }

        // 2. Validate that requested factoryId belongs to this PH
        boolean isAllowed = mappings.stream()
                .anyMatch(m -> m.getFactory().getId().equals(dto.getFactoryId()));

        if (!isAllowed) {
            throw new RuntimeException("You are not authorized to update stock for this factory");
        }

        // 3. Validate product
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 4. Fetch factory
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found"));

        // 5. Find existing stock row
        FactoryInventoryStock existing = factoryInventoryRepository
                .findByFactoryIdAndProductId(factory.getId(), product.getId())
                .orElse(null);

        if (existing == null) {
            // First time entry
            FactoryInventoryStock stock = FactoryInventoryStock.builder()
                    .factory(factory)
                    .product(product)
                    .quantity(dto.getQuantity())
                    .addedBy(plantHead)
                    .build();

            factoryInventoryRepository.save(stock);

        } else {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            existing.setAddedBy(plantHead);
            factoryInventoryRepository.save(existing);
        }

        return new ApiResponseDto<>(true, "Product stock added successfully", null);
    }




    @Transactional
    public ApiResponseDto<Void> addProduction(AddFactoryProductionDto dto, UserPrincipal currentUser) {

        User plantHead = currentUser.getUser();

        // 1️⃣ VALIDATE: Plant Head must belong to this factory
        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(plantHead)
                .orElseThrow(() -> new RuntimeException("User not mapped to a factory"));

        if (!mapping.getFactory().getId().equals(dto.getFactoryId())) {
            return new ApiResponseDto<>(false, "You are not authorized for this factory", null);
        }

        // 2️⃣ VALIDATE: Factory & Product
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new RuntimeException("Factory not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 3️⃣ SAVE PRODUCTION ENTRY
        FactoryProduction production = FactoryProduction.builder()
                .factory(factory)
                .product(product)
                .producedQuantity(dto.getProducedQuantity())
                .build();

        factoryProductionRepository.save(production);

        // 4️⃣ UPDATE INVENTORY STOCK
        FactoryInventoryStock stock =
                factoryInventoryRepository.findByFactoryIdAndProductId(dto.getFactoryId(), dto.getProductId())
                        .orElse(null);

        if (stock == null) {
            // First time entry
            stock = FactoryInventoryStock.builder()
                    .factory(factory)
                    .product(product)
                    .quantity(dto.getProducedQuantity())
                    .addedBy(plantHead)
                    .build();
        } else {
            // Increase inventory
            stock.setQuantity(stock.getQuantity() + dto.getProducedQuantity());
        }

        factoryInventoryRepository.save(stock);

        return new ApiResponseDto<>(true, "Production recorded & inventory updated", null);
    }
}
