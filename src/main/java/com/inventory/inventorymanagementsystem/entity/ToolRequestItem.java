package com.inventory.inventorymanagementsystem.entity;

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
}
