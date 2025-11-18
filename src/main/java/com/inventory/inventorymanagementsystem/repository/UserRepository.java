package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import com.inventory.inventorymanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    List<User> findByRoleAndIsActive(Role role, ActiveStatus status);



    Optional<User> findByEmailIgnoreCase(String email);



    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long excludeId);

    Optional<User> findByTaxRegistrationNumber(String taxRegistrationNumber);
}
