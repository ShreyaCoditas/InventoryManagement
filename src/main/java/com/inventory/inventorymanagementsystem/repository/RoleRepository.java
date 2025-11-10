package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.inventorymanagementsystem.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    @Query(value = "SELECT * FROM roles WHERE role_name = CAST(:roleName AS role_enum)", nativeQuery = true)
    Optional<Role> findByRoleName(@Param("roleName") String roleName);


}
