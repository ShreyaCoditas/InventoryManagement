package com.inventory.inventorymanagementsystem.entity;

import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.Tool;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;


import java.time.ZonedDateTime;

@Entity
@Table(name = "tool_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToolStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @Column(name = "total_quantity", nullable = false)
    private Long totalQuantity = 0L;

    @Column(name = "available_quantity", nullable = false)
    private Long availableQuantity = 0L;

    @Column(name = "issued_quantity", nullable = false)
    private Long issuedQuantity = 0L;

    @LastModifiedDate
    @Column(name = "last_updated_at", nullable = false)
    private ZonedDateTime lastUpdatedAt;
}

