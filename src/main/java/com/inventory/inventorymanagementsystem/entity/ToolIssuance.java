package com.inventory.inventorymanagementsystem.entity;


import com.inventory.inventorymanagementsystem.constants.ToolIssuanceStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_request_id")
    private ToolRequest toolRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id")
    private Tool tool;
//new
    @Column(name = "quantity")
    private Integer quantity;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "issuance_status", )
//    private ToolIssuanceStatus issuanceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "issuance_status", nullable = false)
    private ToolIssuanceStatus issuanceStatus;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "requested_return_quantity")
    private Integer requestedReturnQuantity;


//    @Column(name = "issued_at")
//    private LocalDateTime issuedAt;




    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Bidirectional - One ToolIssuance has Many ToolReturns
    @OneToMany(mappedBy = "toolIssuance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ToolReturn> toolReturns = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }

    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
