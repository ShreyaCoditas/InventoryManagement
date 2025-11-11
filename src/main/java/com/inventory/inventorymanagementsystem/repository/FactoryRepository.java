package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory,Long>, JpaSpecificationExecutor<Factory> {
    // Fetch factories without any Plant Head assigned
    @Query("SELECT f FROM Factory f WHERE f.plantHead IS NULL")
    List<Factory> findUnassignedFactories();
    List<Factory> findByPlantHeadIsNull();

    List<Factory> findByPlantHeadId(Long plantHeadId);
}
