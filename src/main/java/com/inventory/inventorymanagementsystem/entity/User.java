package com.inventory.inventorymanagementsystem.entity;


import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_code", unique = true, length = 20)
    private String userCode;

    @Column(name = "name", unique = true, nullable = false,length = 100)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "profile_image", columnDefinition = "TEXT")
    private String profileImage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password", nullable = false, columnDefinition = "TEXT")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active",nullable=false)
    private ActiveStatus isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One User (Plant Head) has Many Factories
    @OneToMany(mappedBy = "plantHead", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Factory> managedFactories = new ArrayList<>();

    // Bidirectional - One User has Many UserFactoryMappings
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserFactoryMapping> userFactoryMappings = new ArrayList<>();

    // Bidirectional - One User (Central Officer) has Many UserCentralOfficeMappings
    @OneToMany(mappedBy = "centralOfficer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCentralOfficeMapping> userCentralOfficeMappings = new ArrayList<>();

    // Bidirectional - One User (Worker) has Many ToolRequests
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolRequest> toolRequests = new ArrayList<>();

    // Bidirectional - One User (Approver) has Many ApprovedToolRequests
    @OneToMany(mappedBy = "approvedBy", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<ToolRequest> approvedToolRequests = new ArrayList<>();

    // Bidirectional - One User (Worker) has Many ToolExtensions
    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolExtension> toolExtensions = new ArrayList<>();

    // Bidirectional - One User (Approver) has Many ApprovedToolExtensions
    @OneToMany(mappedBy = "approvedBy", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<ToolExtension> approvedToolExtensions = new ArrayList<>();

    // Bidirectional - One User has Many ToolReturns (updated_by)
    @OneToMany(mappedBy = "updatedBy", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<ToolReturn> toolReturns = new ArrayList<>();

    // Bidirectional - One User has Many ToolRestockRequests
    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolRestockRequest> toolRestockRequests = new ArrayList<>();

    // Bidirectional - One User has Many FactoriesInventoryStock (added_by)
    @OneToMany(mappedBy = "addedBy", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<FactoryInventoryStock> addedInventoryStock = new ArrayList<>();

    // Bidirectional - One User (Distributor) has Many DistributorOrders
    @OneToMany(mappedBy = "distributor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DistributorOrder> distributorOrders = new ArrayList<>();

    // Bidirectional - One User (Customer) has Many CustomerDistributorMappings
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerDistributorMapping> customerMappings = new ArrayList<>();

    // Bidirectional - One User (Distributor) has Many DistributorMappings
    @OneToMany(mappedBy = "distributor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerDistributorMapping> distributorMappings = new ArrayList<>();

    // Bidirectional - One User (Customer) has Many Invoices
    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Invoice> customerInvoices = new ArrayList<>();

    // Bidirectional - One User (Distributor) has Many Invoices
    @OneToMany(mappedBy = "distributor", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Invoice> distributorInvoices = new ArrayList<>();

    // Bidirectional - One User (Distributor) has Many DistributorInventories
    @OneToMany(mappedBy = "distributor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DistributorInventory> distributorInventories = new ArrayList<>();

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