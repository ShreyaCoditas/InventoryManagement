package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.FactoryProduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FactoryProductionRepository extends JpaRepository<FactoryProduction, Long> {

    @Query("SELECT COALESCE(SUM(fp.producedQuantity), 0) FROM FactoryProduction fp WHERE fp.factory.id = :factoryId")
    int findTotalProducedQuantityByFactoryId(Long factoryId);

}

