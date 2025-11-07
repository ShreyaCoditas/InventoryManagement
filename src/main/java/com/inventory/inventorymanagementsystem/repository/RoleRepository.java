package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.inventorymanagementsystem.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByRoleName(RoleName roleName);

}
