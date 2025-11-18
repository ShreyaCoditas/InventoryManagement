package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.constants.ActiveStatus;
import com.inventory.inventorymanagementsystem.entity.ToolCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ToolCategoryRepository extends JpaRepository<ToolCategory, Long> {
    Optional<ToolCategory> findByCategoryNameIgnoreCase(String name);

}

