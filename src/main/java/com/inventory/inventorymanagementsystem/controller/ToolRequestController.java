package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.AddToolStockDto;
import com.inventory.inventorymanagementsystem.dto.ApiResponseDto;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.dto.WorkerToolRequestDto;
import com.inventory.inventorymanagementsystem.service.ToolRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ToolRequestController {

    @Autowired
    private  ToolRequestService toolRequestService;

    @PostMapping("tool-request/worker")
    public ResponseEntity<ApiResponseDto<String>> requestTools(
            @RequestBody WorkerToolRequestDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ApiResponseDto<String> response = toolRequestService.createToolRequest(dto, currentUser);
        return ResponseEntity.ok(response);
    }






}
