package com.inventory.inventorymanagementsystem.entity;


import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "image", columnDefinition = "TEXT")
    private String image;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "reward_point")
    private Integer rewardPoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active",nullable=false)
    private ActiveStatus isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One Product has Many FactoryInventoryStocks
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FactoryInventoryStock> factoryInventoryStocks;

    // Bidirectional - One Product has Many FactoryProductions
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FactoryProduction> factoryProductions;

    // Bidirectional - One Product has Many CentralOfficeProductRequests
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CentralOfficeProductRequest> centralOfficeProductRequests;

    // Bidirectional - One Product has Many OrderItems
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    // Bidirectional - One Product has Many DistributorInventories
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DistributorInventory> distributorInventories;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = ActiveStatus.YES;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
