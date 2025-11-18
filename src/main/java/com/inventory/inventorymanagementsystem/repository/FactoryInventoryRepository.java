package com.inventory.inventorymanagementsystem.repository;


import com.inventory.inventorymanagementsystem.entity.FactoryInventoryStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FactoryInventoryRepository extends JpaRepository<FactoryInventoryStock, Long> {


    @Query("SELECT f.product.id, SUM(f.quantity) FROM FactoryInventoryStock f GROUP BY f.product.id")
    List<Object[]> findProductQuantities();

    Optional<FactoryInventoryStock> findByFactoryIdAndProductId(Long factoryId, Long productId);

}

