package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.constants.ToolRequestStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.paginationsortingdto.CSRequestFilterSortDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.PHRequestFilterDto;
import com.inventory.inventorymanagementsystem.paginationsortingdto.WorkerRequestFilterDto;
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
            @Valid @RequestBody WorkerToolRequestDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ApiResponseDto<String> response = toolRequestService.createToolRequest(dto, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/handle/{itemId}/action")
    public ResponseEntity<ApiResponseDto<String>> actOnItem(
            @PathVariable Long itemId,
            @RequestBody ToolItemActionDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(toolRequestService.handleItemAction(itemId, dto, currentUser));
    }

    @GetMapping("/all/cs/requests")
    public ResponseEntity<ApiResponseDto<List<CSRequestItemResponseDto>>> getRequestsForCS(
            @ModelAttribute CSRequestFilterSortDto filter,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApiResponseDto<List<CSRequestItemResponseDto>> response =
                toolRequestService.getRequestsForCS(filter, currentUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all/ph/requests")
    public ResponseEntity<ApiResponseDto<List<PHRequestItemResponseDto>>> getPHRequests(
            PHRequestFilterDto filter,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(toolRequestService.getRequestsForPH(filter, currentUser));
    }

    @GetMapping("/worker/my/requests")
    public ResponseEntity<ApiResponseDto<List<WorkerRequestResponseDto>>> getMyRequests(
            @ModelAttribute WorkerRequestFilterDto filter,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(toolRequestService.getWorkerToolRequestsByStatus(filter, currentUser));
    }

    @PostMapping("/restock")
//    @PreAuthorize("hasRole('CHIEFSUPERVISOR')")
    public ResponseEntity<ApiResponseDto<String>> createRestockRequest(
            @Valid @RequestBody CreateRestockRequestDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(toolRequestService.createRestockRequest(dto, currentUser));
    }


    @GetMapping("/ph/all/restock")
//    @PreAuthorize("hasRole('PLANTHEAD')")
    public ResponseEntity<ApiResponseDto<List<RestockRequestResponseDto>>> getPHRestockRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(toolRequestService.getRestockRequestsForPH(page, size, currentUser));
    }


}


















