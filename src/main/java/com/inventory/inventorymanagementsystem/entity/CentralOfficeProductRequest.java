package com.inventory.inventorymanagementsystem.entity;

import com.inventory.inventorymanagementsystem.constants.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "central_office_product_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentralOfficeProductRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Which Central Office created the request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "central_office_id", nullable = false)
    private CentralOffice centralOffice;

    // Requested product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Factory that needs stock
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    // How many units needed
    @Column(name = "qty_requested", nullable = false)
    private Integer qtyRequested;

    // Who initiated the request (Central Officer user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private User requestedBy;

    // Request status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    // When completed (Plant Head fulfills it)
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Auto timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
