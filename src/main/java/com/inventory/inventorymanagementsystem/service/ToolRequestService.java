package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public ApiResponseDto<String> handleItem(HandleToolRequestItemDto dto, UserPrincipal currentUser) {

        User approver = currentUser.getUser();

        ToolRequestItem item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        Tool tool = item.getTool();

        boolean isExpensive = tool.getIsExpensive() == ExpensiveEnum.YES;

        // ROLE VALIDATION
        if (dto.getAction().equalsIgnoreCase("FORWARD")) {
            if (!approver.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
                throw new RuntimeException("Only Chief Supervisor can forward requests");
            }
        }

        if (dto.getAction().equalsIgnoreCase("APPROVE")) {
            if (isExpensive && !approver.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
                throw new RuntimeException("Only Plant Head can approve expensive tools");
            }
            if (!isExpensive && !approver.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
                throw new RuntimeException("Only Chief Supervisor can approve normal tools");
            }
        }

        if (dto.getAction().equalsIgnoreCase("REJECT")) {
            if (isExpensive && !approver.getRole().getRoleName().equals(RoleName.PLANTHEAD)) {
                throw new RuntimeException("Only Plant Head can reject expensive tools");
            }
            if (!isExpensive && !approver.getRole().getRoleName().equals(RoleName.CHIEFSUPERVISOR)) {
                throw new RuntimeException("Only Chief Supervisor can reject normal tools");
            }
        }

        // GET FACTORY
        UserFactoryMapping mapping = userFactoryMappingRepository.findByUser(approver)
                .orElseThrow(() -> new RuntimeException("Approver not linked to any factory"));

        Factory factory = mapping.getFactory();

        // PROCESS ACTION
        switch (dto.getAction().toUpperCase()) {

            case "APPROVE" -> {
                // Update stock
                ToolStock stock = toolStockRepository.findByToolIdAndFactoryId(tool.getId(), factory.getId())
                        .orElseThrow(() -> new RuntimeException("Tool stock not found in factory"));

                if (stock.getAvailableQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for " + tool.getName());
                }

                stock.setAvailableQuantity(stock.getAvailableQuantity() - item.getQuantity());
                stock.setIssuedQuantity(stock.getIssuedQuantity() + item.getQuantity());
                toolStockRepository.save(stock);

                item.setStatus("APPROVED");
                itemRepository.save(item);

                return new ApiResponseDto<>(true, "Item approved successfully", null);
            }

            case "REJECT" -> {
                if (dto.getReason() == null || dto.getReason().isBlank()) {
                    throw new RuntimeException("Rejection reason is required");
                }

                item.setStatus("REJECTED");
                item.setRejectionReason(dto.getReason());
                itemRepository.save(item);

                return new ApiResponseDto<>(true, "Item rejected successfully", null);
            }

            case "FORWARD" -> {
                item.setStatus("FORWARDED_TO_PH");
                itemRepository.save(item);

                return new ApiResponseDto<>(true, "Item forwarded to Plant Head", null);
            }

            default -> throw new RuntimeException("Invalid action");
        }
    }









}
