package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ToolRepository extends JpaRepository<Tool, Long>, JpaSpecificationExecutor<Tool> {

    boolean existsByNameIgnoreCase(String name);
}
