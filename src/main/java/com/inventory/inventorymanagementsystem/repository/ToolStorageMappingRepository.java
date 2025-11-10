package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.ToolStorageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ToolStorageMappingRepository extends JpaRepository<ToolStorageMapping, Long> {
    int countByFactoryId(Long factoryId);
}

