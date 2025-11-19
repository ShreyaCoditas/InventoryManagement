package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.constants.RequestStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.paginationsortingdto.BaseFilterSortDto;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.ProductRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductRequestController {

    private final ProductRequestService productRequestService;

    // Central Officer creates restock request
//    @PreAuthorize("hasRole('CENTRAL_OFFICER')")
    @PostMapping("/central-office/restock-requests")
    public ResponseEntity<ApiResponseDto<CentralOfficeRestockResponseDto>> createRestockRequest(
            @Valid @RequestBody CreateProductRestockRequestDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApiResponseDto<CentralOfficeRestockResponseDto> response = productRequestService.createRestockRequest(dto, currentUser);
        return ResponseEntity.ok(response);
    }

    // Plant Head completes restock request (factory -> central office)
//    @PreAuthorize("hasRole('PLANTHEAD')")
    @PutMapping("/factories/restock-requests/{requestId}/complete")
    public ResponseEntity<ApiResponseDto<FactoryRestockResponseDto>> completeRestockRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApiResponseDto<FactoryRestockResponseDto> response = productRequestService.completeRestockRequest(requestId, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/plant-head/products/add-stock")
//    @PreAuthorize("hasRole('PLANT_HEAD')")
    public ApiResponseDto<String> addProductStockToFactory(
            @Valid @RequestBody AddProductStockDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return productRequestService.addProductStockToFactory(dto, currentUser);
    }

    // Central Officer: get restock requests created by Central Officer
//    @PreAuthorize("hasRole('CENTRAL_OFFICER')")

    @GetMapping("/central-office/my-restock-requests")
    public ResponseEntity<ApiResponseDto<List<CentralOfficeRestockResponseDto>>> getMyRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            BaseFilterSortDto base,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApiResponseDto<List<CentralOfficeRestockResponseDto>> response =
                productRequestService.getMyRestockRequests(base, currentUser, status);

        return ResponseEntity.ok(response);
    }


    // Plant Head: get restock requests for their assigned factory
//    @PreAuthorize("hasRole('PLANTHEAD')")
    @GetMapping("/factories/my-restock-requests")
    public ResponseEntity<ApiResponseDto<List<FactoryRestockResponseDto>>> getMyFactoryRestockRequests(
            @RequestParam(required = false) RequestStatus status,
            BaseFilterSortDto base,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ApiResponseDto<List<FactoryRestockResponseDto>> response =
                productRequestService.getMyFactoryRestockRequests(base, currentUser, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/production")
    public ResponseEntity<ApiResponseDto<Void>> addProduction(
            @Valid @RequestBody AddFactoryProductionDto dto,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ApiResponseDto<Void> response = productRequestService.addProduction(dto, currentUser);
        return ResponseEntity.ok(response);
    }
}
