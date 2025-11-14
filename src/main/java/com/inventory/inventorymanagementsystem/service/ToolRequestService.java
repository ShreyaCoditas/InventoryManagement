package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ToolRequestService {

    private final UserRepository userRepository;
    private final ToolRepository toolRepository;
    private final ToolRequestRepository requestRepository;
    private final ToolRequestItemRepository itemRepository;
    private final FactoryRepository factoryRepository;
    private final ToolStockRepository toolStockRepository;
    private final UserFactoryMappingRepository userFactoryMappingRepository;


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
        request = requestRepository.save(request);
        List<ToolRequestItem> items = new ArrayList<>();
        for (WorkerToolRequestItemDto itemDto : dto.getItems()) {
            Tool tool = toolRepository.findById(itemDto.getToolId())
                    .orElseThrow(() -> new RuntimeException("Tool not found: " + itemDto.getToolId()));
            ToolStock stock = toolStockRepository.findByToolIdAndFactoryId(
                    tool.getId(), factoryId
            ).orElse(null);
            if (stock == null) {
                return new ApiResponseDto<>(false, "Tool '" + tool.getName() + "' is not available in your factory", null);
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














}
