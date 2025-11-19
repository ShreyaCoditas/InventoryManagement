package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.*;
import com.inventory.inventorymanagementsystem.paginationsortingdto.CSRequestFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.PHRequestFilterDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.WorkerRequestFilterDto;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.specifications.RestockRequestSpecifications;
import com.inventory.inventorymanagementsystem.specifications.ToolRequestItemSpecifications;
import com.inventory.inventorymanagementsystem.specifications.ToolRequestItemWorkerSpecifications;
import com.inventory.inventorymanagementsystem.specifications.ToolRequestSpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolRequestService {

    private final UserRepository userRepository;
    private final ToolRepository toolRepository;
    private final ToolRequestRepository toolRequestRepository;
    private final ToolRequestItemRepository itemRepository;
    private final FactoryRepository factoryRepository;
    private final ToolStockRepository toolStockRepository;
    private final UserFactoryMappingRepository userFactoryMappingRepository;
    private final ToolIssuanceRepository toolIssuanceRepository;
    private final ToolRestockRequestRepository restockRequestRepository;


    // WORKER → creates request
    public ApiResponseDto<String> createToolRequest(WorkerToolRequestDto dto, UserPrincipal currentUser) {
        User worker = currentUser.getUser();
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return new ApiResponseDto<>(false, "No tools selected", null);
        }

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(worker)
                .orElseThrow(() -> new RuntimeException("Worker is not mapped to any factory"));

        Factory workerFactory = mapping.getFactory();
        Long factoryId = workerFactory.getId();
        ToolRequest request = ToolRequest.builder()
                .worker(worker)
                .status(ToolRequestStatus.PENDING)
                .build();
        request = toolRequestRepository.save(request);
        List<ToolRequestItem> items = new ArrayList<>();
        for (WorkerToolRequestItemDto itemDto : dto.getItems()) {
            Tool tool = toolRepository.findById(itemDto.getToolId())
                    .orElseThrow(() -> new RuntimeException("Tool not found: " + itemDto.getToolId()));
            ToolStock stock = toolStockRepository.findByToolIdAndFactoryId(
                    tool.getId(), factoryId
            ).orElse(null);
            if (stock == null) {
                return new ApiResponseDto<>(false, "Tool is not available in your factory", null);
            }
            int available = stock.getAvailableQuantity() != null ? stock.getAvailableQuantity().intValue() : 0;
            if (itemDto.getQuantity() > available) {
                return new ApiResponseDto<>(false, "Requested quantity for '" + tool.getName() +
                        "' exceeds available stock. Available: " + available, null);
            }
            ToolRequestItem item = ToolRequestItem.builder()
                    .toolRequest(request)
                    .tool(tool)
                    .quantity(itemDto.getQuantity())
                    .build();
            items.add(item);
        }
        itemRepository.saveAll(items);
        return new ApiResponseDto<>(true, "Tool request submitted successfully", null);
    }


    public ApiResponseDto<String> handleItemAction(Long itemId, ToolItemActionDto dto, UserPrincipal currentUser) {
        User actingUser = currentUser.getUser();
        ToolRequestItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Tool request item not found"));
        Tool tool = item.getTool();
        boolean isExpensive = tool.getIsExpensive().name().equalsIgnoreCase("YES");
        String role = actingUser.getRole().getRoleName().name();
        String action = dto.getAction().toUpperCase();

        if (role.equals("CHIEFSUPERVISOR")) {
            if (!isExpensive && (action.equals("APPROVE") || action.equals("REJECT"))) {
                if (item.getStatus() != ToolRequestItemStatus.PENDING)
                    throw new RuntimeException("Only PENDING items can be approved/rejected");

                if (action.equals("REJECT") && (dto.getReason() == null || dto.getReason().isBlank()))
                    throw new RuntimeException("Reason is required for rejection");

                if (action.equals("APPROVE"))
                    approveItem(item, actingUser);
                else
                    rejectItem(item, dto.getReason(), actingUser);
                return new ApiResponseDto<>(true, "Action completed", null);
            }

            // CS can only forward expensive tools
            if (isExpensive && action.equals("FORWARD_TO_PH")) {

                if (item.getStatus() != ToolRequestItemStatus.PENDING)
                    throw new RuntimeException("Only PENDING items can be forwarded");

                item.setStatus(ToolRequestItemStatus.FORWARDED_TO_PH);
                itemRepository.save(item);
                return new ApiResponseDto<>(true, "Request forwarded to Plant Head", null);
            }
            throw new RuntimeException("Invalid action for Chief Supervisor");
        }

        if (role.equals("PLANTHEAD")) {

            if (!isExpensive)
                throw new RuntimeException("Plant Head can act only on expensive items");

            if (item.getStatus() != ToolRequestItemStatus.FORWARDED_TO_PH)
                throw new RuntimeException("Only FORWARDED_TO_PH items can be approved by Plant Head");

            if (action.equals("APPROVE")) {
                approveItem(item, actingUser);
                return new ApiResponseDto<>(true, "Approved successfully", null);
            }

            if (action.equals("REJECT")) {

                if (dto.getReason() == null || dto.getReason().isBlank())
                    throw new RuntimeException("Reason is required for rejection");

                rejectItem(item, dto.getReason(), actingUser);
                return new ApiResponseDto<>(true, "Rejected successfully", null);
            }
            throw new RuntimeException("Invalid action for Plant Head");
        }
        throw new RuntimeException("Unauthorized role");
    }

    public ApiResponseDto<List<CSRequestItemResponseDto>> getRequestsForCS(
            CSRequestFilterSortDto filter,
            UserPrincipal currentUser
    ) {
        User cs = currentUser.getUser();


        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(cs)
                .orElseThrow(() -> new RuntimeException("Supervisor not mapped to any factory"));
        Long factoryId = mapping.getFactory().getId();


        Sort sort = "desc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.by(filter.getSortBy()).descending()
                : Sort.by(filter.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<ToolRequestItem> spec = Specification.allOf(
                ToolRequestItemSpecifications.belongsToFactory(factoryId),
                ToolRequestItemSpecifications.hasStatus(filter.getStatus()),
                ToolRequestItemSpecifications.hasExpensiveTag(filter.getExpensive())
        );


        Page<ToolRequestItem> page = itemRepository.findAll(spec, pageable);


        List<CSRequestItemResponseDto> dtos = page.getContent().stream()
                .map(item -> {
                    Tool tool = item.getTool();
                    User worker = item.getToolRequest().getWorker();
                    String tag = tool.getIsExpensive() == ExpensiveEnum.YES ? "Expensive" : "Inexpensive";
                    Integer availableQty = toolStockRepository.sumAvailableQuantityByToolId(tool.getId());

                    return new CSRequestItemResponseDto(
                            item.getId(),
                            item.getToolRequest().getId(),
                            tool.getName(),
                            tool.getImageUrl(),
                            worker.getUsername(),
                            worker.getProfileImage(),
                            item.getQuantity(),
                            availableQty != null ? availableQty : 0,
                            //item.getStorageAreaCode(), // R1C1S1B1
                            tag,
                            item.getStatus().name()
                    );
                })
                .toList();

        Map<String, Object> pagination = PaginationUtil.build(page);
        return new ApiResponseDto<>(true, "Requests fetched successfully", dtos, pagination);
    }


    public ApiResponseDto<List<PHRequestItemResponseDto>> getRequestsForPH(
            PHRequestFilterDto filter,
            UserPrincipal currentUser
    ) {
        User ph = currentUser.getUser();

        // find factory where PH is assigned
        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(ph)
                .orElseThrow(() -> new RuntimeException("PH is not mapped to any factory"));

        Long factoryId = mapping.getFactory().getId();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(),
                Sort.by("id").descending());  // fixed sorting

        Specification<ToolRequestItem> spec = Specification
                .allOf(ToolRequestSpecifications.belongsToFactory(factoryId))
                .and(ToolRequestSpecifications.expensiveOnly())
                .and(ToolRequestSpecifications.hasStatus(filter.getStatus()));

        Page<ToolRequestItem> page = itemRepository.findAll(spec, pageable);

        List<PHRequestItemResponseDto> dtos = page.getContent().stream()
                .map(item -> {
                    Tool tool = item.getTool();
                    User worker = item.getToolRequest().getWorker();

                    Integer availableQty =
                            toolStockRepository.sumAvailableQuantityByToolId(tool.getId());
                    if (availableQty == null) availableQty = 0;

                    return new PHRequestItemResponseDto(
                            item.getId(),
                            item.getToolRequest().getId(),
                            tool.getName(),
                            tool.getImageUrl(),
                            worker.getUsername(),
                            worker.getProfileImage(),
                            item.getQuantity(),
                            availableQty,
                            "Expensive",
                            item.getStatus().name()
                    );
                })
                .toList();

        Map<String, Object> pagination = PaginationUtil.build(page);

        return new ApiResponseDto<>(true, "Requests fetched", dtos, pagination);
    }

    public ApiResponseDto<List<WorkerRequestResponseDto>> getWorkerToolRequestsByStatus(
            WorkerRequestFilterDto filter,
            UserPrincipal currentUser
    ) {
        User worker = currentUser.getUser();

        // Only worker can call this API
        if (!worker.getRole().getRoleName().equals(RoleName.WORKER)) {
            throw new RuntimeException("Only workers can view their requests");
        }

        Long workerId = worker.getId();

        // ===== SORTING =====
        Sort sort = "desc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.by(filter.getSortBy()).descending()
                : Sort.by(filter.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        // ===== SPECIFICATION =====
        Specification<ToolRequestItem> spec = Specification.allOf(
                ToolRequestItemWorkerSpecifications.belongsToWorker(workerId),
                ToolRequestItemWorkerSpecifications.hasStatus(filter.getStatus()),
                ToolRequestItemWorkerSpecifications.searchByToolName(filter.getSearch())
        );

        Page<ToolRequestItem> page = itemRepository.findAll(spec, pageable);

        // ===== DTO MAPPING =====
        List<WorkerRequestResponseDto> dtos = page.getContent().stream()
                .map(item -> {

                    Tool tool = item.getTool();

                    // Convert status enum → user-friendly string
                    String mappedStatus = switch (item.getStatus()) {
                        case PENDING -> "Pending";
                        case APPROVED -> "Allocated";
                        case REJECTED -> "Rejected";
                        default -> "Pending";
                    };

                    // Compute return window (non-perishable → 30 days)
                    String returnWindow =
                            tool.getIsPerishable() == IsPerishableEnum.NO
                                    ? "30 days"
                                    : "-";

                    String reason = null;

                    if (item.getStatus() == ToolRequestItemStatus.REJECTED) {
                        reason = item.getRejectionReason();
                    }


                    return new WorkerRequestResponseDto(
                            item.getId(),
                            tool.getName(),
                            tool.getImageUrl(),
                            item.getQuantity(),
                            item.getToolRequest().getCreatedAt().toString(),
                            returnWindow,
                            mappedStatus,
                            reason
                    );
                })
                .toList();

        Map<String, Object> pagination = PaginationUtil.build(page);

        return new ApiResponseDto<>(true, "Worker requests fetched successfully", dtos, pagination);
    }

    @Transactional
    public ApiResponseDto<String> createRestockRequest(CreateRestockRequestDto dto, UserPrincipal currentUser) {

        User cs = currentUser.getUser();

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(cs)
                .orElseThrow(() -> new RuntimeException("Supervisor not mapped to any factory"));

        Factory factory = mapping.getFactory();


        Tool tool = toolRepository.findById(dto.getToolId())
                .orElseThrow(() -> new RuntimeException("Tool not found"));


        Optional<ToolStock> stockOpt =
                toolStockRepository.findByToolIdAndFactoryId(tool.getId(), factory.getId());

        if (stockOpt.isEmpty()) {
            return new ApiResponseDto<>(
                    false,
                    "Tool '" + tool.getName() + "' is not associated with your factory",
                    null
            );
        }


        ToolRestockRequest request = ToolRestockRequest.builder()
                .tool(tool)
                .factory(factory)
                .requestedBy(cs)
                .createdAt(LocalDateTime.now())
                .status(RestockStatus.PENDING)
                .build();

        restockRequestRepository.save(request);

        return new ApiResponseDto<>(true, "Restock request created successfully", null);
    }


    @Transactional
    public ApiResponseDto<List<RestockRequestResponseDto>> getRestockRequestsForPH(
            int page, int size, UserPrincipal currentUser) {

        User ph = currentUser.getUser();

        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(ph)
                .orElseThrow(() -> new RuntimeException("Plant Head not mapped to factory"));

        Long factoryId = mapping.getFactory().getId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<ToolRestockRequest> spec = Specification.allOf(
                RestockRequestSpecifications.belongsToFactory(factoryId)
        );

        Page<ToolRestockRequest> reqPage = restockRequestRepository.findAll(spec, pageable);

        List<RestockRequestResponseDto> dtos = reqPage.getContent().stream()
                .map(r -> {

                    ToolStock stock = toolStockRepository
                            .findByToolIdAndFactoryId(r.getTool().getId(), factoryId)
                            .orElse(null);

                    Long total = stock != null ? stock.getTotalQuantity() : 0L;
                    Long available = stock != null ? stock.getAvailableQuantity() : 0L;

                    return new RestockRequestResponseDto(
                            r.getId(),
                            r.getTool().getId(),
                            r.getTool().getName(),
                            r.getTool().getImageUrl(),
                            factoryId,
                            mapping.getFactory().getName(),
                            r.getRequestedBy().getUsername(),
                            total,
                            available
                    );
                })
                .toList();

        Map<String, Object> pagination = PaginationUtil.build(reqPage);

        return new ApiResponseDto<>(true, "Restock requests fetched", dtos, pagination);
    }



    // --------------------------
    // HELPER: APPROVE
    // --------------------------
    private void approveItem(ToolRequestItem item, User approver) {

        // decrease stock
        Long factoryId = userFactoryMappingRepository.findByUserId(approver.getId())
                .orElseThrow(() -> new RuntimeException("User not mapped to a factory"))
                .getFactory().getId();

        ToolStock stock = toolStockRepository.findByToolIdAndFactoryId(
                item.getTool().getId(), factoryId
        ).orElseThrow(() -> new RuntimeException("Tool not found in factory stock"));

        if (stock.getAvailableQuantity() < item.getQuantity())
            throw new RuntimeException("Not enough stock to approve");

        stock.setAvailableQuantity(stock.getAvailableQuantity() - item.getQuantity());
        stock.setIssuedQuantity(stock.getIssuedQuantity() + item.getQuantity());
        toolStockRepository.save(stock);

        item.setStatus(ToolRequestItemStatus.APPROVED);
        itemRepository.save(item);
        ToolIssuance issuance = ToolIssuance.builder()
                .toolRequest(item.getToolRequest())
                .tool(item.getTool())
                .quantity(item.getQuantity())
                .issuanceStatus(ToolIssuanceStatus.ALLOCATED)
                .build();

        toolIssuanceRepository.save(issuance);
    }

    // --------------------------
    // HELPER: REJECT
    // --------------------------
    private void rejectItem(ToolRequestItem item, String reason, User approver) {
        item.setStatus(ToolRequestItemStatus.REJECTED);
        item.setRejectionReason(reason);
        itemRepository.save(item);
    }


}
