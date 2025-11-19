package com.inventory.inventorymanagementsystem.service;

import com.inventory.inventorymanagementsystem.constants.OrderStatus;
import com.inventory.inventorymanagementsystem.constants.PaymentStatus;
import com.inventory.inventorymanagementsystem.dto.*;
import com.inventory.inventorymanagementsystem.entity.*;
import com.inventory.inventorymanagementsystem.repository.*;
import com.inventory.inventorymanagementsystem.specifications.CentralInventorySpecifications;
import com.inventory.inventorymanagementsystem.util.PaginationUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class OrderService {
    private final CentralOfficeInventoryRepository centralOfficeInventoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final DistributorOrderRepository distributorOrderRepository;
    private final OrderBatchRepository orderBatchRepository;
    private final InvoiceRepository invoiceRepository;

    private static final int MIN_PCT = 15;
    private static final int MAX_PCT = 35;


    public ApiResponseDto<?> getAllForDistributor(
            String search,
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<CentralOfficeInventory> spec = Specification
                .allOf(CentralInventorySpecifications.nameContains(search))
                .and(CentralInventorySpecifications.categoryEquals(categoryId))
                .and(CentralInventorySpecifications.minPrice(minPrice))
                .and(CentralInventorySpecifications.maxPrice(maxPrice));

        Page<CentralOfficeInventory> result = centralOfficeInventoryRepository.findAll(spec, pageable);

        List<CentralInventoryProductDto> mapped = result.stream()
                .map(this::toCentralInventoryDto)
                .collect(Collectors.toList());

        return new ApiResponseDto<>(true, "Products fetched successfully", mapped, PaginationUtil.build(result));
    }


    @Transactional
    public ApiResponseDto<DistributorPlaceOrderResponseDto> placeOrder(Long distributorId,
                                                                       DistributorPlaceOrderRequestDto dto) {
        User distributor = userRepository.findById(distributorId)
                .orElseThrow(() -> new EntityNotFoundException("Distributor not found"));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));


        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(dto.getQuantity())
                .pricePerUnit(product.getPrice())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        orderItem = orderItemRepository.save(orderItem);


        DistributorOrder order = DistributorOrder.builder()
                .distributor(distributor)
                .orderItem(orderItem)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity())))
                .orderCode(generateOrderCode())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        DistributorOrder saved = distributorOrderRepository.save(order);

        DistributorPlaceOrderResponseDto response = new DistributorPlaceOrderResponseDto();
        response.setOrderId(saved.getId());
        response.setOrderCode(saved.getOrderCode());
        response.setProductName(product.getName());
        response.setQuantity(dto.getQuantity());
        response.setPrice(product.getPrice().doubleValue());
        response.setTotalPrice(saved.getTotalPrice().doubleValue());
        response.setStatus(saved.getStatus().name());

        return new ApiResponseDto<>(true, "Order placed successfully", response);
    }


    @Transactional
    public ApiResponseDto<?> updateOrderStatus(Long centralOfficerId, Long orderId, OrderStatusUpdateDto dto) {

        DistributorOrder order = distributorOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));


        if (order.getStatus() != OrderStatus.PENDING) {
            return new ApiResponseDto<>(false, "Order already processed by another officer", null);
        }

        String action = dto.getAction() == null ? "" : dto.getAction().trim().toUpperCase();
        switch (action) {
            case "ACCEPT":
                // nothing else to change for now; keep PENDING until dispatch
                order.setStatus(OrderStatus.APPROVED);
                order.setUpdatedAt(LocalDateTime.now());
                distributorOrderRepository.save(order);
                return new ApiResponseDto<>(true, "Order accepted");
            case "REJECT":
                if (dto.getReason() == null || dto.getReason().isBlank()) {
                    return new ApiResponseDto<>(false, "Rejection reason is required", null);
                }
                order.setStatus(OrderStatus.REJECTED);
                order.setRejectReason(dto.getReason());
                order.setUpdatedAt(LocalDateTime.now());
                distributorOrderRepository.save(order);
                return new ApiResponseDto<>(true, "Order rejected");
            default:
                return new ApiResponseDto<>(false, "Invalid action. Use ACCEPT or REJECT", null);
        }
    }

    @Transactional
    public ApiResponseDto<DispatchResponseDto> dispatchOrder(Long centralOfficerId, Long orderId) {

        DistributorOrder order = distributorOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.REJECTED) {
            return new ApiResponseDto<>(false, "Cannot dispatch a rejected order", null);
        }
        if (order.getStatus() == OrderStatus.FULFILLED) {
            return new ApiResponseDto<>(false, "Order is already fulfilled", null);
        }

        OrderItem item = order.getOrderItem();
        if (item == null) {
            return new ApiResponseDto<>(false, "Order has no item to dispatch", null);
        }

        long requestedQtyLong = item.getQuantity() != null ? item.getQuantity() : 0;
        int requestedQty = (int) requestedQtyLong;

        // already dispatched for this order item
        int alreadySent = orderBatchRepository.findByOrderItem_Id(item.getId())
                .stream().mapToInt(b -> b.getQuantitySent() != null ? b.getQuantitySent() : 0).sum();

        int remainingToSend = requestedQty - alreadySent;
        if (remainingToSend <= 0) {
            return new ApiResponseDto<>(false, "Order already fully dispatched", null);
        }

        CentralOfficeInventory inv = centralOfficeInventoryRepository.findAll().stream()
                .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
                .findFirst()
                .orElse(null);

        if (inv == null) {
            return new ApiResponseDto<>(false, "No inventory record found for product", null);
        }

        int available = inv.getQuantity() != null ? inv.getQuantity().intValue() : 0;
        if (available <= 0) {
            return new ApiResponseDto<>(false, "No stock available at Central Office", null);
        }

        int sendNow = Math.min(available, remainingToSend);
        List<Integer> batches = (available >= remainingToSend)
                ? List.of(remainingToSend)
                : splitRandom(sendNow, remainingToSend, MIN_PCT, MAX_PCT);

        int totalSentNow = 0;
        List<SimpleBatchDto> batchDtos = new ArrayList<>();

        for (Integer qty : batches) {
            OrderBatch batch = OrderBatch.builder()
                    .order(order)
                    .orderItem(item)
                    .quantitySent(qty)
                    .sentAt(LocalDateTime.now())
                    .batchReference("BATCH-" + UUID.randomUUID().toString().substring(0, 8))
                    .build();
            orderBatchRepository.save(batch);
            inv.setQuantity(inv.getQuantity() - qty);
            centralOfficeInventoryRepository.save(inv);

            SimpleBatchDto bd = new SimpleBatchDto();
            bd.setBatchRef(batch.getBatchReference());
            bd.setQuantity(qty);
            batchDtos.add(bd);
            totalSentNow += qty;
        }


        BigDecimal unitPrice = item.getPricePerUnit() != null ? item.getPricePerUnit() : BigDecimal.ZERO;
        BigDecimal invoiceAmount = unitPrice.multiply(BigDecimal.valueOf(totalSentNow));

        Invoice invoice = Invoice.builder()
                .order(order)
                .distributor(order.getDistributor())
                .invoiceDate(LocalDate.now())
                .totalAmount(invoiceAmount)
                .createdAt(LocalDateTime.now())
                .build();
        invoiceRepository.save(invoice);
        int totalFulfilled = alreadySent + totalSentNow;
        if (totalFulfilled >= requestedQty) {
            order.setStatus(OrderStatus.FULFILLED);
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_FULFILLED);
            order.setPaymentStatus(PaymentStatus.UNPAID);
        }

        order.setInvoiceId(invoice.getId());
        order.setUpdatedAt(LocalDateTime.now());
        distributorOrderRepository.save(order);

        // prepare response DTO
        DispatchResponseDto resp = new DispatchResponseDto();
        resp.setOrderId(order.getId());
        resp.setOrderCode(order.getOrderCode());
        resp.setRequestedQuantity(requestedQty);
        resp.setTotalSent(totalSentNow);
        resp.setRemaining(requestedQty - totalFulfilled);
        resp.setStatus(order.getStatus().name());
        resp.setPaymentStatus(order.getPaymentStatus().name());

        InvoiceSimpleDto invDto = new InvoiceSimpleDto();
        invDto.setInvoiceId(invoice.getId());
        invDto.setTotalAmount(invoice.getTotalAmount());
        invDto.setBatchesCount(batchDtos.size());
        invDto.setBatches(batchDtos);

        resp.setInvoice(invDto);
        return new ApiResponseDto<>(true, "Dispatch completed", resp);
    }


    public ApiResponseDto<OrderDetailsResponseDto> getDistributorOrderDetails(Long distributorId, Long orderId) {

        DistributorOrder order = distributorOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getDistributor().getId().equals(distributorId)) {
            return new ApiResponseDto<>(false, "You are not authorized to view this order", null);
        }

        OrderItem item = order.getOrderItem();
        Product product = item.getProduct();

        List<OrderBatch> batches = orderBatchRepository.findByOrder_Id(orderId);

        int totalDispatched = batches.stream()
                .mapToInt(b -> b.getQuantitySent() != null ? b.getQuantitySent() : 0)
                .sum();

        int requested = item.getQuantity();
        int remaining = requested - totalDispatched;

        List<OrderBatchDto> batchDtos = batches.stream().map(b -> {
            OrderBatchDto dto = new OrderBatchDto();
            dto.setBatchReference(b.getBatchReference());
            dto.setQuantity(b.getQuantitySent());
            dto.setSentAt(b.getSentAt() != null ? b.getSentAt().toString() : null);
            return dto;
        }).collect(Collectors.toList());

        OrderDetailsResponseDto resp = new OrderDetailsResponseDto();
        resp.setOrderId(order.getId());
        resp.setOrderCode(order.getOrderCode());

        resp.setProductId(product.getId());
        resp.setProductName(product.getName());
        resp.setProductImage(product.getImage());
        resp.setPricePerUnit(item.getPricePerUnit());

        resp.setRequestedQuantity(requested);
        resp.setTotalDispatched(totalDispatched);
        resp.setRemainingQuantity(remaining);

        resp.setOrderStatus(order.getStatus() != null ? order.getStatus().name() : null);
        resp.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);

        resp.setOrderDate(order.getCreatedAt());
        resp.setInvoiceId(order.getInvoiceId());

        if (order.getInvoiceId() != null) {
            invoiceRepository.findById(order.getInvoiceId()).ifPresent(inv -> resp.setInvoiceAmount(inv.getTotalAmount()));
        }

        resp.setBatches(batchDtos);

        return new ApiResponseDto<>(true, "Order details fetched", resp);
    }

    // -----------------------
    // Helpers
    // -----------------------
    private CentralInventoryProductDto toCentralInventoryDto(CentralOfficeInventory inv) {
        CentralInventoryProductDto dto = new CentralInventoryProductDto();
        dto.setProductId(inv.getProduct().getId());
        dto.setName(inv.getProduct().getName());
        dto.setImage(inv.getProduct().getImage());
        dto.setCategoryName(inv.getProduct().getCategory() != null ? inv.getProduct().getCategory().getCategoryName() : null);
        dto.setPrice(inv.getProduct().getPrice() != null ? inv.getProduct().getPrice().doubleValue() : null);
        dto.setAvailableQuantity(inv.getQuantity());
        return dto;
    }

    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<Integer> splitRandom(int available, int remaining, int minPct, int maxPct) {
        List<Integer> list = new ArrayList<>();
        Random random = new Random();

        int left = available;
        int rem = remaining;

        while (left > 0) {
            int pct = minPct + random.nextInt(Math.max(1, maxPct - minPct + 1));
            int qty = (int) Math.round(rem * (pct / 100.0));

            if (qty < 1) qty = 1;
            if (qty > left) qty = left;

            list.add(qty);
            left -= qty;
            rem -= qty;
        }
        return list;
    }
}
