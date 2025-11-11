package com.inventory.inventorymanagementsystem.entity;




import com.inventory.inventorymanagementsystem.constants.RoleName;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "user_factory_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFactoryMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "bay_id", length = 50)
    private String bayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factory_id")
    private Factory factory;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_role")
    private RoleName assignedRole;

    public UserFactoryMapping(User user, Factory factory, RoleName assignedRole) {
        this.user = user;
        this.factory = factory;
        this.assignedRole = assignedRole;
    }
}