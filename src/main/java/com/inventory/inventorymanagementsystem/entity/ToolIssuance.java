package com.inventory.inventorymanagementsystem.entity;


import com.inventory.inventorymanagementsystem.constants.ToolIssuanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tool_issuance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolIssuance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_request_id")
    private ToolRequest toolRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id")
    private Tool tool;

    @Enumerated(EnumType.STRING)
    @Column(name = "issuance_status", columnDefinition = "tool_issuance_status")
    private ToolIssuanceStatus issuanceStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One ToolIssuance has Many ToolReturns
    @OneToMany(mappedBy = "toolIssuance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolReturn> toolReturns;

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
