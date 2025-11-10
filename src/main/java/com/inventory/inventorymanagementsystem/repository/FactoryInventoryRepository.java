package com.inventory.inventorymanagementsystem.repository;


import com.inventory.inventorymanagementsystem.entity.FactoryInventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FactoryInventoryRepository extends JpaRepository<FactoryInventoryStock, Long> {

    @Query("SELECT SUM(f.quantity) FROM FactoryInventoryStock f WHERE f.product.id = :productId")
    Integer findTotalQuantityByProductId(@Param("productId") Long productId);

    @Query("SELECT f FROM FactoryInventoryStock f WHERE f.product.id = :productId")
    List<FactoryInventoryStock> findByProductId(@Param("productId") Long productId);

    // ✅ Total quantity of a product in a specific factory
    @Query("SELECT SUM(f.quantity) FROM FactoryInventoryStock f WHERE f.product.id = :productId AND f.factory.id = :factoryId")
    Integer findTotalQuantityByProductIdAndFactoryId(@Param("productId") Long productId, @Param("factoryId") Long factoryId);
}

