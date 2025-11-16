package com.inventory.inventorymanagementsystem.repository;

import com.inventory.inventorymanagementsystem.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory,Long>, JpaSpecificationExecutor<Factory> {
    // Fetch factories without any Plant Head assigned
    @Query("SELECT f FROM Factory f WHERE f.plantHead IS NULL")
    List<Factory> findUnassignedFactories();
    List<Factory> findByPlantHeadIsNull();

    List<Factory> findByPlantHeadId(Long plantHeadId);

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT f.name FROM Factory f WHERE f.plantHead.id = :plantHeadId")
    List<String> findFactoryNamesByPlantHeadId(@Param("plantHeadId") Long plantHeadId);

}
