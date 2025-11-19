package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.RequestStatus;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.exceptions.CustomException;
import com.inventory.inventorymanagementsystem.exceptions.ResourceNotFoundException;
import com.inventory.inventorymanagementsystem.paginationsortingdto.BaseFilterSortDto;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.specifications.CentralOfficeInventorySpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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


    public ApiResponseDto<CentralOfficeRestockResponseDto> createRestockRequest(
            CreateProductRestockRequestDto dto,
            UserPrincipal currentUser
    ) {
        User user = currentUser.getUser();
        if (user.getRole() == null || user.getRole().getRoleName() == null ||
                !user.getRole().getRoleName().name().equalsIgnoreCase("CENTRALOFFICER")) {
            throw new AccessDeniedException("Only central officers can create restock requests");
        }
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));


        CentralOfficeProductRequest request = CentralOfficeProductRequest.builder()
                .factory(factory)
                .product(product)
                .qtyRequested(dto.getQtyRequested())
                .status(RequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        try {
            request.setRequestedBy(user);
        } catch (Exception ignore) {}

        requestRepo.save(request);
        return new ApiResponseDto<>(true, "Restock request created", null);
    }


    @Transactional
    public ApiResponseDto<FactoryRestockResponseDto> completeRestockRequest(
            Long requestId, UserPrincipal currentUser) {

        User user = currentUser.getUser();

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("User not mapped to a factory", HttpStatus.NOT_FOUND));
        Long userFactoryId = mapping.getFactory().getId();


        CentralOfficeProductRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new CustomException("Restock request not found",HttpStatus.NOT_FOUND));

        if (!Objects.equals(request.getFactory().getId(), userFactoryId)) {
            throw new AccessDeniedException("Not authorized to complete this restock request");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new CustomException("Only PENDING requests can be completed",HttpStatus.NOT_FOUND);
        }

        Product product = request.getProduct();
        long qty = request.getQtyRequested() != null ? request.getQtyRequested() : 0;

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
        request.setStatus(RequestStatus.COMPLETED);
        request.setUpdatedAt(LocalDateTime.now());
        request.setCompletedAt(LocalDateTime.now());
        requestRepo.save(request);

        return new ApiResponseDto<>(true, "Restock request completed", null);
    }


    @Transactional
    public ApiResponseDto<List<CentralOfficeRestockResponseDto>> getMyRestockRequests(
            BaseFilterSortDto base,
            UserPrincipal currentUser,
            RequestStatus statusFilter
    ) {
        User centralOfficer = currentUser.getUser();
        Sort sort = "desc".equalsIgnoreCase(base.getSortDirection())
                ? Sort.by(base.getSortBy()).descending()
                : Sort.by(base.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(base.getPage(), base.getSize(), sort);
        Specification<CentralOfficeProductRequest> spec = (root, query, cb) ->
                cb.equal(root.get("requestedBy").get("id"), centralOfficer.getId());

        if (statusFilter != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), statusFilter));
        }

        Page<CentralOfficeProductRequest> page = requestRepo.findAll(spec, pageable);
        List<CentralOfficeRestockResponseDto> dtos = page.getContent().stream().map(r -> {
            CentralOfficeRestockResponseDto dto = new CentralOfficeRestockResponseDto();
            dto.setId(r.getId());
            dto.setQtyRequested(r.getQtyRequested());
            dto.setStatus(r.getStatus());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setCompletedAt(r.getCompletedAt());

            if (r.getProduct() != null) {
                dto.setProductId(r.getProduct().getId());
                dto.setProductName(r.getProduct().getName());
                dto.setProductImageUrl(r.getProduct().getImage());
            }

            if (r.getFactory() != null) {
                dto.setFactoryId(r.getFactory().getId());
                dto.setFactoryName(r.getFactory().getName());
            }

            if (r.getRequestedBy() != null) {
                dto.setRequestedByUserId(r.getRequestedBy().getId());
                dto.setRequestedByUserName(r.getRequestedBy().getUsername());
            }

            return dto;
        }).toList();

        Map<String, Object> pagination = PaginationUtil.build(page);

        return new ApiResponseDto<>(true, "My restock requests fetched successfully", dtos, pagination);
    }

    @Transactional(readOnly = true)
    public ApiResponseDto<List<FactoryRestockResponseDto>> getMyFactoryRestockRequests(
            BaseFilterSortDto base,
            UserPrincipal currentUser,
            RequestStatus statusFilter
    ) {

        User user = currentUser.getUser();

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("User not mapped to any factory"));

        Long factoryId = mapping.getFactory().getId();
        Sort sort = "desc".equalsIgnoreCase(base.getSortDirection())
                ? Sort.by(base.getSortBy()).descending()
                : Sort.by(base.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(base.getPage(), base.getSize(), sort);
        Specification<CentralOfficeProductRequest> spec =
                (root, query, cb) -> cb.equal(root.get("factory").get("id"), factoryId);
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

            if (r.getRequestedBy() != null) {
                dto.setRequestedByUserId(r.getRequestedBy().getId());
                dto.setRequestedByUserName(r.getRequestedBy().getUsername());
            }
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
        List<UserFactoryMapping> mappings =
                userFactoryMappingRepository.findAllByUser(plantHead);

        if (mappings.isEmpty()) {
            throw new RuntimeException("Plant Head is not assigned to any factory");
        }

        boolean isAllowed = mappings.stream()
                .anyMatch(m -> m.getFactory().getId().equals(dto.getFactoryId()));

        if (!isAllowed) {
            throw new CustomException("You are not authorized to update stock for this factory",HttpStatus.FORBIDDEN);
        }

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        FactoryInventoryStock existing = factoryInventoryRepository
                .findByFactoryIdAndProductId(factory.getId(), product.getId())
                .orElse(null);

        if (existing == null) {

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

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(plantHead)
                .orElseThrow(() -> new CustomException("User not mapped to a factory",HttpStatus.NOT_FOUND));

        if (!mapping.getFactory().getId().equals(dto.getFactoryId())) {
            return new ApiResponseDto<>(false, "You are not authorized for this factory", null);
        }
        Factory factory = factoryRepository.findById(dto.getFactoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        FactoryProduction production = FactoryProduction.builder()
                .factory(factory)
                .product(product)
                .producedQuantity(dto.getProducedQuantity())
                .build();

        factoryProductionRepository.save(production);

        FactoryInventoryStock stock =
                factoryInventoryRepository.findByFactoryIdAndProductId(dto.getFactoryId(), dto.getProductId())
                        .orElse(null);

        if (stock == null) {

            stock = FactoryInventoryStock.builder()
                    .factory(factory)
                    .product(product)
                    .quantity(dto.getProducedQuantity())
                    .addedBy(plantHead)
                    .build();
        } else {

            stock.setQuantity(stock.getQuantity() + dto.getProducedQuantity());
        }

        factoryInventoryRepository.save(stock);

        return new ApiResponseDto<>(true, "Production recorded & inventory updated", null);
    }
}
