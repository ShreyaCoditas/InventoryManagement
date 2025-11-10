package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.entity.UserFactoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFactoryMappingRepository extends JpaRepository<UserFactoryMapping,Long>, JpaSpecificationExecutor<UserFactoryMapping> {
    //  Get all supervisors (or other users) by role
    List<UserFactoryMapping> findByAssignedRole(RoleName assignedRole);
    int countByFactoryIdAndAssignedRole(Long factoryId, RoleName assignedRole);

    // ✅ Find the factory ID where a user is assigned (used for Chief Supervisor)
    @Query("SELECT ufm.factory.id FROM UserFactoryMapping ufm WHERE ufm.user.id = :userId")
    Optional<Long> findFactoryIdByUserId(@Param("userId") Long userId);

    // ✅ Count workers assigned to a bay
    @Query("SELECT COUNT(ufm) FROM UserFactoryMapping ufm WHERE ufm.bayId = :bayId AND ufm.assignedRole = :role")
    long countByBayIdAndAssignedRole(@Param("bayId") Long bayId, @Param("role") RoleName role);




    //  Optional: find by factory ID and role
    List<UserFactoryMapping> findByFactoryIdAndAssignedRole(Long factoryId, RoleName assignedRole);

    boolean existsByFactory_IdAndAssignedRole(Long factoryId, RoleName assignedRole);

}
