package com.inventory.inventorymanagementsystem.entity;
import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.IsPerishableEnum;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "tool")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ToolCategory category;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "tool_description", columnDefinition = "TEXT")
    private String toolDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_perishable",nullable = false )
    private IsPerishableEnum isPerishable;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_expensive",nullable = false )
    private ExpensiveEnum isExpensive;

    @Column(name = "threshold")
    private Integer threshold;

//    @Column(name = "available_quantity")
//    private Integer availableQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active",length = 20, nullable = false)
    private ActiveStatus isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One Tool has Many StorageAreas
    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StorageArea> storageAreas = new ArrayList<>();

    // Bidirectional - One Tool has Many ToolStorageMappings
    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolStorageMapping> toolStorageMappings = new ArrayList<>();
//
//    // Bidirectional - One Tool has Many ToolRequests
//    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<ToolRequest> toolRequests = new ArrayList<>();

    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolRequestItem> toolRequestItems = new ArrayList<>();


    // Bidirectional - One Tool has Many ToolIssuances
    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolIssuance> toolIssuances = new ArrayList<>();

    // Bidirectional - One Tool has Many ToolExtensions
    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolExtension> toolExtensions = new ArrayList<>();

    // Bidirectional - One Tool has Many ToolRestockRequests
    @OneToMany(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolRestockRequest> toolRestockRequests = new ArrayList<>();

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