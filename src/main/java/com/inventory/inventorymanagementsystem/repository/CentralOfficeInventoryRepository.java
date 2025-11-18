package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.CentralOfficeInventory;
import com.inventory.inventorymanagementsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CentralOfficeInventoryRepository extends JpaRepository<CentralOfficeInventory, Long>,
        JpaSpecificationExecutor<CentralOfficeInventory> {
    Optional<CentralOfficeInventory> findByProduct(Product product);
}

