package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.constants.ExpensiveEnum;
import com.inventory.inventorymanagementsystem.constants.IsPerishableEnum;
import com.inventory.inventorymanagementsystem.entity.Tool;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long>, JpaSpecificationExecutor<Tool> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Tool> findByIdAndIsActive(Long id, ActiveStatus isActive);

    Page<Tool> findByIsActive(ActiveStatus activeStatus, Pageable pageable);


}
