package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.ToolRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tool-request")
@RequiredArgsConstructor
public class ToolRequestController {

    @Autowired
    private  ToolRequestService toolRequestService;

    @PostMapping("/worker")
    public ResponseEntity<ApiResponseDto<String>> requestTools(
            @RequestBody WorkerToolRequestDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ApiResponseDto<String> response = toolRequestService.createToolRequest(dto, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/handle")
    public ResponseEntity<ApiResponseDto<String>> handleItem(
            @RequestBody HandleToolRequestItemDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(
                toolRequestService.handleItem(dto, currentUser)
        );
    }















//
//    @PutMapping("/handle")
//    public ResponseEntity<ApiResponseDto<String>> handleItem(
//            @RequestBody HandleToolRequestItemDto dto,
//            @AuthenticationPrincipal UserPrincipal currentUser
//    ) {
//        ApiResponseDto<String> response = toolRequestService.handleItem(dto, currentUser);
//        return ResponseEntity.ok(response);
//    }
//
//    // List items for CS
//    @GetMapping("/cs/items")
//    public ResponseEntity<ApiResponseDto<List<ToolRequestItemResponseDto>>> csItems(
//            @AuthenticationPrincipal UserPrincipal currentUser
//    ) {
//        return ResponseEntity.ok(toolRequestService.getItemsForCS(currentUser));
//    }

//    // List items for PH
//    @GetMapping("/ph/items")
//    public ResponseEntity<ApiResponseDto<List<ToolRequestItemResponseDto>>> phItems(
//            @AuthenticationPrincipal UserPrincipal currentUser
//    ) {
//        return ResponseEntity.ok(toolRequestService.getItemsForPH(currentUser));
//    }













}
