//package com.inventory.inventorymanagementsystem.service;
//
//import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
//import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
//import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
//import com.inventory.inventorymanagementsystem.dto.ToolDto;
//import com.inventory.inventorymanagementsystem.dto.ToolRequestDto;
//import com.inventory.inventorymanagementsystem.entity.Tool;
//import com.inventory.inventorymanagementsystem.entity.ToolRequest;
//import com.inventory.inventorymanagementsystem.entity.User;
//import com.inventory.inventorymanagementsystem.repository.*;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.time.LocalDateTime;
//
//public class WorkerService {
//
//    @Autowired
//    private ToolRequestRepository toolRequestRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ToolRepository toolRepository;
//
//
//    @Transactional
//    public ApiResponseDto<String> requestTool(Long workerId, ToolRequestDto request) {
//        User worker = userRepository.findById(workerId)
//                .orElseThrow(() -> new RuntimeException("Worker not found"));
//        Tool tool = toolRepository.findById(request.getToolId())
//                .orElseThrow(() -> new RuntimeException("Tool not found"));
//        if (request.getRequestQuantity() > tool.getAvailableQuantity()) {
//            return new ApiResponseDto<>(false, "Quantity demanded is more than available stock", null);
//        }
//        ToolRequest toolRequest = new ToolRequest();
//        toolRequest.setTool(tool);
//        toolRequest.setWorker(worker);
//        toolRequest.setRequestQuantity(request.getRequestQuantity());
//        toolRequest.setStatus(ToolRequestStatus.PENDING);
//        toolRequest.setCreatedAt(LocalDateTime.now());
//        toolRequest.setUpdatedAt(LocalDateTime.now());
//
//        // Expensive → Plant Head approval, else → Chief Supervisor
//        if (tool.getIsExpensive() == ExpensiveEnum.YES) {
//            toolRequest.setApprovedBy(null); // PlantHead will approve later
//        }
//        toolRequestRepository.save(toolRequest);
//        return new ApiResponseDto<>(true, "Tool request submitted successfully", null);
//    }
//
//
//}
