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

    @Query("SELECT f.product.id, SUM(f.quantity) " +
            "FROM FactoryInventoryStock f " +
            "WHERE f.product.id IN :ids " +
            "GROUP BY f.product.id")
    List<Object[]> findQuantitiesForProducts(@Param("ids") List<Long> ids);

    @Query("SELECT COALESCE(SUM(f.quantity), 0) FROM FactoryInventoryStock f WHERE f.factory.id = :factoryId")
    int getTotalProductsByFactoryId(@Param("factoryId") Long factoryId);



}

