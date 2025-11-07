package com.inventory.inventorymanagementsystem.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "central_office")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CentralOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One CentralOffice has Many UserCentralOfficeMapping
    @OneToMany(mappedBy = "centralOffice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCentralOfficeMapping> userCentralOfficeMappings;

    // Bidirectional - One CentralOffice has Many Factories
    @OneToMany(mappedBy = "centralOffice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Factory> factories;

    // Bidirectional - One CentralOffice has Many CentralOfficeProductRequests
    @OneToMany(mappedBy = "centralOffice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CentralOfficeProductRequest> productRequests;

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
