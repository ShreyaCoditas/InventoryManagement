package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.StorageArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StorageAreaRepository extends JpaRepository<StorageArea, Long> {

    @Query("SELECT COALESCE(SUM(s.currentQuantity), 0) FROM StorageArea s WHERE s.tool.id = :toolId")
    int findTotalAvailableQuantityByToolId(Long toolId);
}
