package com.inventory.inventorymanagementsystem.entity;

import com.inventory.inventorymanagementsystem.constants.ToolRequestItemStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool_request_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent ToolRequest
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_request_id", nullable = false)
    private ToolRequest toolRequest;

    // Tool requested
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    // Quantity for THIS tool
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

//    @Builder.Default
//    @Column(name = "status")
//    private String status = "PENDING";

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ToolRequestItemStatus status = ToolRequestItemStatus.PENDING;

    @Builder.Default
    @Column(name = "rejection_reason")
    private String rejectionReason = null;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = ToolRequestItemStatus.PENDING;
        }
    }

}
