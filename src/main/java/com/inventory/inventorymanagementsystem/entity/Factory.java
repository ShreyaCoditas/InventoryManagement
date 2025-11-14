package com.inventory.inventorymanagementsystem.entity;




import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "factories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "factory_code", unique = true, length = 20)
    private String factoryCode;

    @Column(name = "name",unique = true, length = 100)
    private String name;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planthead_id")
    private User plantHead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "central_office_id")
    private CentralOffice centralOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active")
    private ActiveStatus isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One Factory has Many UserFactoryMappings
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserFactoryMapping> userFactoryMappings;

    // Bidirectional - One Factory has Many Bays
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bay> bays;

    // Bidirectional - One Factory has Many StorageAreas
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StorageArea> storageAreas;

    // Bidirectional - One Factory has Many ToolStorageMappings
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolStorageMapping> toolStorageMappings;

    // Bidirectional - One Factory has Many ToolRestockRequests
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolRestockRequest> toolRestockRequests;

    // Bidirectional - One Factory has Many FactoryInventoryStocks
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FactoryInventoryStock> factoryInventoryStocks;

    // Bidirectional - One Factory has Many FactoryProductions
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FactoryProduction> factoryProductions;

    // Bidirectional - One Factory has Many CentralOfficeProductRequests
    @OneToMany(mappedBy = "factory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CentralOfficeProductRequest> centralOfficeProductRequests;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = ActiveStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}