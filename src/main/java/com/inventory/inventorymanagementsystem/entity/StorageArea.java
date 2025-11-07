package com.inventory.inventorymanagementsystem.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "storage_area")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "column_number")
    private Integer columnNumber;

    @Column(name = "stack_level")
    private Integer stackLevel;

    @Column(name = "bucket_number")
    private Integer bucketNumber;

    @Column(name = "current_quantity")
    private Integer currentQuantity;

    @Column(name = "maximum_quantity")
    private Integer maximumQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One StorageArea has Many ToolStorageMappings
    @OneToMany(mappedBy = "storageArea", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolStorageMapping> toolStorageMappings;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}