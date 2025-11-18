package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.RoleName;
import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.User;
import com.inventory.inventorymanagementsystem.entity.UserFactoryMapping;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFactoryMappingRepository extends JpaRepository<UserFactoryMapping,Long>, JpaSpecificationExecutor<UserFactoryMapping> {
    @Transactional
    @Modifying
    @Query("DELETE FROM UserFactoryMapping ufm WHERE ufm.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    Optional<UserFactoryMapping> findByUser(User user);


    List<UserFactoryMapping> findAllByUser(User user);
    @Query("SELECT f.name FROM UserFactoryMapping ufm " +
            "JOIN ufm.factory f " +
            "WHERE ufm.user.id = :userId")
    List<String> findFactoryNamesByUserId(@Param("userId") Long userId);

    Optional<UserFactoryMapping> findByFactoryIdAndUserRoleRoleName(Long factoryId, RoleName roleName);








    //  Get all supervisors (or other users) by role
    List<UserFactoryMapping> findByAssignedRole(RoleName assignedRole);
    int countByFactoryIdAndAssignedRole(Long factoryId, RoleName assignedRole);

    // Find the factory ID where a user is assigned (used for Chief Supervisor)
    @Query("SELECT ufm.factory.id FROM UserFactoryMapping ufm WHERE ufm.user.id = :userId")
    Optional<Long> findFactoryIdByUserId(@Param("userId") Long userId);

    //  Count workers assigned to a bay
    @Query("SELECT COUNT(ufm) FROM UserFactoryMapping ufm WHERE ufm.bayId = :bayId AND ufm.assignedRole = :role")
    long countByBayIdAndAssignedRole(@Param("bayId") Long bayId, @Param("role") RoleName role);

    @Query("SELECT u.username FROM UserFactoryMapping ufm " +
            "JOIN ufm.user u " +
            "WHERE ufm.factory.id = :factoryId AND ufm.assignedRole = 'CHIEFSUPERVISOR'")
    List<String> findChiefSupervisorsByFactoryId(@Param("factoryId") Long factoryId);





    //  Optional: find by factory ID and role
    List<UserFactoryMapping> findByFactoryIdAndAssignedRole(Long factoryId, RoleName assignedRole);

    boolean existsByFactory_IdAndAssignedRole(Long factoryId, RoleName assignedRole);




    long countByBayIdAndAssignedRole(String bayId, RoleName role);
    Optional<UserFactoryMapping> findByUserId(Long userId);


    boolean existsByUserIdAndFactoryIdAndAssignedRole(Long id, Long id1, RoleName roleName);

    @Query("SELECT ufm.factory FROM UserFactoryMapping ufm WHERE ufm.user.id = :userId")
    Optional<Factory> findFactoryByUserId(@Param("userId") Long userId);

    @Query("SELECT ufm.user FROM UserFactoryMapping ufm " +
            "WHERE ufm.factory.id = :factoryId " +
            "AND ufm.assignedRole = com.inventory.inventorymanagementsystem.constants.RoleName.CHIEFSUPERVISOR")
    Optional<User> findChiefSupervisorByFactoryId(@Param("factoryId") Long factoryId);

}
