package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.ToolRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolRequestItemRepository extends JpaRepository<ToolRequestItem,Long> {
}
