package com.inventory.inventorymanagementsystem.controller;

import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.security.UserPrincipal;
import com.inventory.inventorymanagementsystem.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ------------------------------------------------------------
    // 1) Get all central office products (Distributor)
    // ------------------------------------------------------------
    @GetMapping("/all")
    public ApiResponseDto<?> getCentralProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "product.name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return orderService.getAllForDistributor(
                search, categoryId, minPrice, maxPrice,
                page, size, sortBy, direction
        );
    }

    // ------------------------------------------------------------
    // 2) Distributor places an order
    // ------------------------------------------------------------
    @PostMapping("/distributor/orders")
    public ApiResponseDto<?> placeOrder(
            @Valid @RequestBody DistributorPlaceOrderRequestDto dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long distributorId = principal.getUser().getId();
        return orderService.placeOrder(distributorId, dto);
    }

    // ------------------------------------------------------------
    // 3) Central Officer Accepts/Rejects order
    // ------------------------------------------------------------
    @PostMapping("/{orderId}/update-status")
    public ApiResponseDto<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateDto dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long officerId = principal.getUser().getId();
        return orderService.updateOrderStatus(officerId, orderId, dto);
    }

    // ------------------------------------------------------------
    // 4) Central Officer Dispatches Order
    // ------------------------------------------------------------
    @PostMapping("/{orderId}/dispatch")
    public ApiResponseDto<?> dispatchOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long officerId = principal.getUser().getId();
        return orderService.dispatchOrder(officerId, orderId);
    }

    // ------------------------------------------------------------
    // 5) Distributor views order details + batch history
    // ------------------------------------------------------------
    @GetMapping("/distributor/orders/{orderId}")
    public ApiResponseDto<?> getOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long distributorId = principal.getUser().getId();
        return orderService.getDistributorOrderDetails(distributorId, orderId);
    }
}
