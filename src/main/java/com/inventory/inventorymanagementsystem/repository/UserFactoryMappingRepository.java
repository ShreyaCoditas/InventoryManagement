package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.entity.UserFactoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFactoryMappingRepository extends JpaRepository<UserFactoryMapping,Long> {
    //  Get all supervisors (or other users) by role
    List<UserFactoryMapping> findByAssignedRole(RoleName assignedRole);

    //  Optional: find by factory ID and role
    List<UserFactoryMapping> findByFactoryIdAndAssignedRole(Long factoryId, RoleName assignedRole);
}
