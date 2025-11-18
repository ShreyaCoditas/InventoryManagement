package com.inventory.inventorymanagementsystem.repository;
import com.inventory.inventorymanagementsystem.entity.Tool;
import com.inventory.inventorymanagementsystem.entity.Factory;
import com.inventory.inventorymanagementsystem.entity.ToolStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolStockRepository extends JpaRepository<ToolStock, Long> {


    @Query("SELECT SUM(ts.totalQuantity) FROM ToolStock ts WHERE ts.tool.id = :toolId")
    Integer sumTotalQuantityByToolId(@Param("toolId") Long toolId);

    @Query("SELECT SUM(ts.availableQuantity) FROM ToolStock ts WHERE ts.tool.id = :toolId")
    Integer sumAvailableQuantityByToolId(@Param("toolId") Long toolId);

    Optional<ToolStock> findByFactoryAndTool(Factory factory, Tool tool);
    Optional<ToolStock> findByToolIdAndFactoryId(Long toolId, Long factoryId);




}

